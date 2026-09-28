package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neo4j.driver.Record;
import org.neo4j.driver.Values;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.domain.valueobject.RuleKind;
import com.zera.ms_inventory.core.domain.valueobject.RuleLimitUnit;
import com.zera.ms_inventory.core.domain.valueobject.RuleTargetType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleRepositoryImplTest {

    @Mock private org.springframework.data.neo4j.core.Neo4jClient neo4jClient;

    private Record row(String targetLabel, String targetId, String limitUnit, Object limitValue) {
        Record linha = mock(Record.class);
        when(linha.get("id")).thenReturn(Values.value(UUID.randomUUID().toString()));
        when(linha.get("unitId")).thenReturn(Values.value(Fixtures.UNIT.toString()));
        when(linha.get("name")).thenReturn(Values.value("Garantia"));
        when(linha.get("kind")).thenReturn(Values.value("WARRANTY_EXPIRATION"));
        when(linha.get("limitValue")).thenReturn(limitValue == null ? Values.NULL : Values.value((int) limitValue));
        when(linha.get("limitUnit")).thenReturn(limitUnit == null ? Values.NULL : Values.value(limitUnit));
        when(linha.get("active")).thenReturn(Values.value(true));
        when(linha.get("createdAt")).thenReturn(Values.value(LocalDateTime.of(2026, 9, 20, 10, 0)));
        when(linha.get("updatedAt")).thenReturn(Values.value(LocalDateTime.of(2026, 9, 21, 10, 0)));
        when(linha.get("targetLabel")).thenReturn(targetLabel == null ? Values.NULL : Values.value(targetLabel));
        when(linha.get("targetId")).thenReturn(targetId == null ? Values.NULL : Values.value(targetId));
        return linha;
    }

    /** O rotulo do no alvo vira o tipo do alvo: :Model e MODEL, :Category e CATEGORY. */
    @Test
    void shouldMapARuleTargetingAModel() {
        UUID modelId = UUID.randomUUID();

        Rule regra = RuleRepositoryImpl.toRule(null, row("Model", modelId.toString(), "DAYS", 30));

        assertThat(regra.getUnitId()).isEqualTo(Fixtures.UNIT);
        assertThat(regra.getKind()).isEqualTo(RuleKind.WARRANTY_EXPIRATION);
        assertThat(regra.getLimitValue()).isEqualTo(30);
        assertThat(regra.getLimitUnit()).isEqualTo(RuleLimitUnit.DAYS);
        assertThat(regra.getTarget().type()).isEqualTo(RuleTargetType.MODEL);
        assertThat(regra.getTarget().id()).isEqualTo(modelId);
        assertThat(regra.appliesToWholeUnit()).isFalse();
        assertThat(regra.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 9, 20, 10, 0));
    }

    @Test
    void shouldMapARuleTargetingACategory() {
        UUID categoryId = UUID.randomUUID();

        Rule regra = RuleRepositoryImpl.toRule(null, row("Category", categoryId.toString(), "DAYS", 30));

        assertThat(regra.getTarget().type()).isEqualTo(RuleTargetType.CATEGORY);
        assertThat(regra.getTarget().id()).isEqualTo(categoryId);
    }

    /** Sem relacao APPLIES_TO, a regra vale para a unidade inteira. */
    @Test
    void shouldMapAWholeUnitRule() {
        Rule regra = RuleRepositoryImpl.toRule(null, row(null, null, "DAYS", 30));

        assertThat(regra.getTarget()).isNull();
        assertThat(regra.appliesToWholeUnit()).isTrue();
    }

    /** Regra que so liga e desliga nao tem limite, e isso nao pode quebrar a leitura. */
    @Test
    void shouldMapARuleWithoutLimit() {
        Record linha = row(null, null, null, null);
        when(linha.get("kind")).thenReturn(Values.value("RECYCLABLE_TO_LANDFILL"));

        Rule regra = RuleRepositoryImpl.toRule(null, linha);

        assertThat(regra.getKind()).isEqualTo(RuleKind.RECYCLABLE_TO_LANDFILL);
        assertThat(regra.getLimitValue()).isNull();
        assertThat(regra.getLimitUnit()).isNull();
    }

    // ---- execucao das consultas ----

    @Mock private org.springframework.data.neo4j.core.Neo4jClient.UnboundRunnableSpec spec;

    private RuleRepositoryImpl repository() {
        return new RuleRepositoryImpl(neo4jClient);
    }

    @SuppressWarnings("unchecked")
    private <T> org.mockito.ArgumentCaptor<java.util.Map<String, Object>> stubFetchAs(
            Class<T> tipo, java.util.List<T> resultados) {
        var mapping = mock(org.springframework.data.neo4j.core.Neo4jClient.MappingSpec.class);
        var fetch = mock(org.springframework.data.neo4j.core.Neo4jClient.RecordFetchSpec.class);
        when(neo4jClient.query(org.mockito.ArgumentMatchers.anyString())).thenReturn(spec);
        when(spec.bindAll(org.mockito.ArgumentMatchers.anyMap())).thenReturn(spec);
        when(spec.fetchAs(tipo)).thenReturn(mapping);
        when(mapping.mappedBy(org.mockito.ArgumentMatchers.any())).thenReturn(fetch);
        org.mockito.Mockito.lenient().when(fetch.one())
                .thenReturn(resultados.isEmpty() ? java.util.Optional.empty()
                        : java.util.Optional.of(resultados.get(0)));
        org.mockito.Mockito.lenient().when(fetch.all()).thenReturn(resultados);
        return org.mockito.ArgumentCaptor.forClass(java.util.Map.class);
    }

    private Rule regra(com.zera.ms_inventory.core.domain.valueobject.RuleTarget alvo) {
        return new Rule(UUID.randomUUID(), Fixtures.UNIT, "Garantia", RuleKind.WARRANTY_EXPIRATION, 30,
                RuleLimitUnit.DAYS, alvo, true);
    }

    /** O alvo vira rotulo do no: o Cypher precisa de :Model e :Category, nao de MODEL e CATEGORY. */
    @Test
    void shouldBindTheTargetAsTheNodeLabel() {
        UUID modelId = UUID.randomUUID();
        Rule regra = regra(com.zera.ms_inventory.core.domain.valueobject.RuleTarget.model(modelId));
        var parametros = stubFetchAs(Rule.class, java.util.List.of(regra));

        repository().save(regra);

        org.mockito.Mockito.verify(spec).bindAll(parametros.capture());
        assertThat(parametros.getValue()).containsEntry("targetLabel", "Model")
                .containsEntry("targetId", modelId.toString())
                .containsEntry("unitId", Fixtures.UNIT.toString())
                .containsEntry("kind", "WARRANTY_EXPIRATION")
                .containsEntry("limitUnit", "DAYS");
    }

    @Test
    void shouldBindACategoryTargetAndAWholeUnitRule() {
        UUID categoryId = UUID.randomUUID();
        Rule comCategoria = regra(
                com.zera.ms_inventory.core.domain.valueobject.RuleTarget.category(categoryId));
        var parametros = stubFetchAs(Rule.class, java.util.List.of(comCategoria));

        repository().save(comCategoria);
        org.mockito.Mockito.verify(spec).bindAll(parametros.capture());
        assertThat(parametros.getValue()).containsEntry("targetLabel", "Category");

        repository().save(regra(null));
        org.mockito.Mockito.verify(spec, org.mockito.Mockito.times(2)).bindAll(parametros.capture());
        assertThat(parametros.getValue()).containsEntry("targetLabel", null)
                .containsEntry("targetId", null);
    }

    @Test
    void shouldFailLoudlyWhenTheSaveReturnsNothing() {
        stubFetchAs(Rule.class, java.util.List.of());

        Rule regra = regra(null);
        RuleRepositoryImpl repository = repository();

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> repository.save(regra))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(regra.getId().toString());
    }

    @Test
    void shouldSaveAllInOrder() {
        Rule primeira = regra(null);
        Rule segunda = regra(null);
        stubFetchAs(Rule.class, java.util.List.of(primeira));

        assertThat(repository().saveAll(java.util.List.of(primeira, segunda))).hasSize(2);
    }

    @Test
    void shouldFindByIdAndListWithinTheUnit() {
        Rule regra = regra(null);
        stubFetchAs(Rule.class, java.util.List.of(regra));

        assertThat(repository().findById(Fixtures.UNIT, regra.getId())).contains(regra);
        assertThat(repository().findAll(Fixtures.UNIT)).containsExactly(regra);
    }

    @Test
    void shouldCountTheRulesOfTheUnit() {
        stubFetchAs(Long.class, java.util.List.of(8L));

        assertThat(repository().countByUnit(Fixtures.UNIT)).isEqualTo(8L);
    }

    @Test
    void shouldReturnZeroWhenTheUnitHasNoRules() {
        stubFetchAs(Long.class, java.util.List.of());

        assertThat(repository().countByUnit(Fixtures.UNIT)).isZero();
    }

    @Test
    void shouldDeleteWithinTheUnit() {
        UUID id = UUID.randomUUID();
        when(neo4jClient.query(org.mockito.ArgumentMatchers.anyString())).thenReturn(spec);
        when(spec.bindAll(org.mockito.ArgumentMatchers.anyMap())).thenReturn(spec);

        repository().deleteById(Fixtures.UNIT, id);

        org.mockito.Mockito.verify(spec).run();
    }
}
