package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neo4j.driver.Record;
import org.neo4j.driver.Values;
import org.springframework.data.neo4j.core.Neo4jClient;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.domain.valueobject.AlertSubject;
import com.zera.ms_inventory.core.domain.valueobject.RuleKind;
import com.zera.ms_inventory.core.domain.valueobject.RuleLimitUnit;
import com.zera.ms_inventory.core.domain.valueobject.RuleTarget;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleEvaluationRepositoryImplTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 9, 26);

    @Mock private Neo4jClient neo4jClient;
    @Mock private Neo4jClient.UnboundRunnableSpec spec;

    @SuppressWarnings("unchecked")
    private <T> ArgumentCaptor<String> stubFetchAs(Class<T> tipo, List<T> resultados) {
        Neo4jClient.MappingSpec<T> mapping = mock(Neo4jClient.MappingSpec.class);
        Neo4jClient.RecordFetchSpec<T> fetch = mock(Neo4jClient.RecordFetchSpec.class);
        ArgumentCaptor<String> cypher = ArgumentCaptor.forClass(String.class);
        when(neo4jClient.query(anyString())).thenReturn(spec);
        org.mockito.Mockito.lenient().when(spec.bindAll(anyMap())).thenReturn(spec);
        when(spec.fetchAs(tipo)).thenReturn(mapping);
        when(mapping.mappedBy(any())).thenReturn(fetch);
        org.mockito.Mockito.lenient().when(fetch.all()).thenReturn(resultados);
        org.mockito.Mockito.lenient().when(fetch.one())
                .thenReturn(resultados.isEmpty() ? Optional.empty() : Optional.of(resultados.get(0)));
        return cypher;
    }

    private Rule rule(RuleKind kind, Integer limite, RuleLimitUnit unidade, RuleTarget alvo) {
        return new Rule(UUID.randomUUID(), Fixtures.UNIT, kind.name(), kind, limite, unidade, alvo, true);
    }

    private RuleEvaluationRepositoryImpl repository() {
        return new RuleEvaluationRepositoryImpl(neo4jClient);
    }

    @Test
    void shouldMapAViolatingItem() {
        UUID itemId = UUID.randomUUID();
        Record linha = mock(Record.class);
        when(linha.get("itemId")).thenReturn(Values.value(itemId.toString()));
        when(linha.get("displayCode")).thenReturn(Values.value("100001"));
        when(linha.get("name")).thenReturn(Values.value("Notebook"));

        AlertSubject subject = RuleEvaluationRepositoryImpl.toSubject(null, linha);

        assertThat(subject.itemId()).isEqualTo(itemId);
        assertThat(subject.displayCode()).isEqualTo("100001");
        assertThat(subject.name()).isEqualTo("Notebook");
    }

    @Test
    void shouldMapAnItemWithoutShortCodeOrName() {
        Record linha = mock(Record.class);
        when(linha.get("itemId")).thenReturn(Values.value(UUID.randomUUID().toString()));
        when(linha.get("displayCode")).thenReturn(Values.NULL);
        when(linha.get("name")).thenReturn(Values.NULL);

        AlertSubject subject = RuleEvaluationRepositoryImpl.toSubject(null, linha);

        assertThat(subject.displayCode()).isNull();
        assertThat(subject.name()).isNull();
    }

    /** A regra de ocupacao e da unidade e a do descarte e sincrona: nenhuma das duas consulta itens. */
    @Test
    void shouldNotQueryForRulesWithoutAnItemCondition() {
        RuleEvaluationRepositoryImpl repository = repository();

        assertThat(repository.findViolatingItems(
                rule(RuleKind.STOCK_QUANTITY_LIMIT, 90, RuleLimitUnit.PERCENT, null), HOJE)).isEmpty();
        assertThat(repository.findViolatingItems(
                rule(RuleKind.RECYCLABLE_TO_LANDFILL, null, null, null), HOJE)).isEmpty();
        verify(neo4jClient, never()).query(anyString());
    }

    @Test
    void shouldQueryEachTemporalRuleWithItsCutoff() {
        stubFetchAs(AlertSubject.class, List.of());

        repository().findViolatingItems(rule(RuleKind.WARRANTY_EXPIRATION, 30, RuleLimitUnit.DAYS, null),
                HOJE);

        ArgumentCaptor<Map<String, Object>> parametros = ArgumentCaptor.forClass(Map.class);
        verify(spec).bindAll(parametros.capture());
        assertThat(parametros.getValue())
                .containsEntry("unitId", Fixtures.UNIT.toString())
                .containsEntry("forward", LocalDate.of(2026, 10, 26))
                .containsEntry("targetId", null);
    }

    @Test
    void shouldNarrowTheQueryToTheRuleTarget() {
        UUID modelId = UUID.randomUUID();
        stubFetchAs(AlertSubject.class, List.of());

        repository().findViolatingItems(
                rule(RuleKind.STALE_ITEM, 6, RuleLimitUnit.MONTHS, RuleTarget.model(modelId)), HOJE);

        ArgumentCaptor<Map<String, Object>> parametros = ArgumentCaptor.forClass(Map.class);
        verify(spec).bindAll(parametros.capture());
        assertThat(parametros.getValue())
                .containsEntry("targetType", "MODEL")
                .containsEntry("targetId", modelId.toString())
                .containsEntry("backward", LocalDate.of(2026, 3, 26));
    }

    @Test
    void shouldBindTheRawLimitForUsageIntensity() {
        stubFetchAs(AlertSubject.class, List.of());

        repository().findViolatingItems(rule(RuleKind.USAGE_INTENSITY_LIMIT, 8, RuleLimitUnit.UNITS, null),
                HOJE);

        ArgumentCaptor<Map<String, Object>> parametros = ArgumentCaptor.forClass(Map.class);
        verify(spec).bindAll(parametros.capture());
        // intensidade compara numero, nao data
        assertThat(parametros.getValue()).containsEntry("limit", 8)
                .containsEntry("forward", null).containsEntry("backward", null);
    }

    @Test
    void shouldListTheUnitsThatHaveRules() {
        stubFetchAs(UUID.class, List.of(Fixtures.UNIT, Fixtures.OTHER_UNIT));

        assertThat(repository().unitsWithRules()).containsExactly(Fixtures.UNIT, Fixtures.OTHER_UNIT);
    }

    @Test
    void shouldCountTheActiveItemsOfTheUnit() {
        stubFetchAs(Long.class, List.of(42L));

        assertThat(repository().countActiveItems(Fixtures.UNIT)).isEqualTo(42L);
    }

    @Test
    void shouldReturnZeroActiveItemsWithoutRows() {
        stubFetchAs(Long.class, List.of());

        assertThat(repository().countActiveItems(Fixtures.UNIT)).isZero();
    }
}
