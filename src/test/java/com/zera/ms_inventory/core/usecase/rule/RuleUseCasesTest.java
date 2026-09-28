package com.zera.ms_inventory.core.usecase.rule;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.domain.exception.RuleNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.RuleKind;
import com.zera.ms_inventory.core.domain.valueobject.RuleLimitUnit;
import com.zera.ms_inventory.core.domain.valueobject.RuleTarget;
import com.zera.ms_inventory.core.repository.CategoryRepository;
import com.zera.ms_inventory.core.repository.ModelRepository;
import com.zera.ms_inventory.core.repository.RuleRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleUseCasesTest {

    @Mock private RuleRepository ruleRepository;
    @Mock private ModelRepository modelRepository;
    @Mock private CategoryRepository categoryRepository;

    private RuleTargetResolver resolver() {
        return new RuleTargetResolver(modelRepository, categoryRepository);
    }

    private Rule rule(UUID id) {
        return new Rule(id, Fixtures.UNIT, "Garantia", RuleKind.WARRANTY_EXPIRATION, 30,
                RuleLimitUnit.DAYS, null, true);
    }

    // ---- criacao ----

    @Test
    void shouldCreateTheRuleInTheUnit() {
        UUID modelId = UUID.randomUUID();
        when(modelRepository.findById(Fixtures.UNIT, modelId))
                .thenReturn(Optional.of(Fixtures.model(modelId, Fixtures.UNIT)));
        when(ruleRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Rule criada = new CreateRuleImpl(ruleRepository, resolver()).execute(new CreateRuleCommand(Fixtures.UNIT,
                "Garantia", RuleKind.WARRANTY_EXPIRATION, 30, RuleLimitUnit.DAYS,
                RuleTarget.model(modelId), true));

        assertThat(criada.getUnitId()).isEqualTo(Fixtures.UNIT);
        assertThat(criada.getTarget().id()).isEqualTo(modelId);
        assertThat(criada.getId()).isNotNull();
    }

    // ---- semeadura das regras padrao ----

    /** Primeiro acesso da unidade: o alerta precisa funcionar sem ninguem configurar nada. */
    @Test
    void shouldSeedTheDefaultRulesOnTheFirstListing() {
        when(ruleRepository.countByUnit(Fixtures.UNIT)).thenReturn(0L);
        when(ruleRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        List<Rule> regras = new FindAllRulesImpl(ruleRepository).execute(Fixtures.UNIT);

        assertThat(regras).hasSize(RuleKind.values().length);
        ArgumentCaptor<List<Rule>> semeadas = ArgumentCaptor.forClass(List.class);
        verify(ruleRepository).saveAll(semeadas.capture());
        assertThat(semeadas.getValue()).allMatch(r -> r.getUnitId().equals(Fixtures.UNIT));
        verify(ruleRepository, never()).findAll(any());
    }

    @Test
    void shouldNotSeedWhenTheUnitAlreadyHasRules() {
        when(ruleRepository.countByUnit(Fixtures.UNIT)).thenReturn(3L);
        when(ruleRepository.findAll(Fixtures.UNIT)).thenReturn(List.of(rule(UUID.randomUUID())));

        List<Rule> regras = new FindAllRulesImpl(ruleRepository).execute(Fixtures.UNIT);

        assertThat(regras).hasSize(1);
        verify(ruleRepository, never()).saveAll(any());
    }

    // ---- consulta e exclusao ----

    @Test
    void shouldFindTheRuleInTheUnit() {
        UUID id = UUID.randomUUID();
        when(ruleRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(rule(id)));

        assertThat(new FindRuleByIdImpl(ruleRepository).execute(Fixtures.UNIT, id).getId()).isEqualTo(id);
    }

    @Test
    void shouldNotSeeARuleFromAnotherUnit() {
        UUID id = UUID.randomUUID();
        when(ruleRepository.findById(Fixtures.OTHER_UNIT, id)).thenReturn(Optional.empty());
        FindRuleByIdImpl useCase = new FindRuleByIdImpl(ruleRepository);

        assertThatThrownBy(() -> useCase.execute(Fixtures.OTHER_UNIT, id))
                .isInstanceOf(RuleNotFoundException.class);
    }

    @Test
    void shouldNotDeleteARuleFromAnotherUnit() {
        UUID id = UUID.randomUUID();
        when(ruleRepository.findById(Fixtures.OTHER_UNIT, id)).thenReturn(Optional.empty());
        DeleteRuleImpl useCase = new DeleteRuleImpl(ruleRepository);

        assertThatThrownBy(() -> useCase.execute(Fixtures.OTHER_UNIT, id))
                .isInstanceOf(RuleNotFoundException.class);
        verify(ruleRepository, never()).deleteById(any(), any());
    }

    // ---- edicoes ----

    @Test
    void shouldRenameChangeLimitTargetAndActivation() {
        UUID id = UUID.randomUUID();
        Rule regra = rule(id);
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findById(Fixtures.UNIT, categoryId))
                .thenReturn(Optional.of(Fixtures.category(categoryId, Fixtures.UNIT)));
        when(ruleRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(regra));
        when(ruleRepository.save(regra)).thenReturn(regra);
        UpdateRuleUseCases useCases = new UpdateRuleUseCases(ruleRepository, resolver());

        useCases.execute(Fixtures.UNIT, id, "Outro nome");
        useCases.execute(Fixtures.UNIT, id, 60, RuleLimitUnit.DAYS);
        useCases.execute(Fixtures.UNIT, id, RuleTarget.category(categoryId));
        useCases.execute(Fixtures.UNIT, id, false);

        assertThat(regra.getName()).isEqualTo("Outro nome");
        assertThat(regra.getLimitValue()).isEqualTo(60);
        assertThat(regra.getTarget().id()).isEqualTo(categoryId);
        assertThat(regra.isActive()).isFalse();

        useCases.execute(Fixtures.UNIT, id, (RuleTarget) null);
        assertThat(regra.appliesToWholeUnit()).isTrue();

        useCases.execute(Fixtures.UNIT, id, true);
        assertThat(regra.isActive()).isTrue();
    }

    @Test
    void shouldNotEditARuleFromAnotherUnit() {
        UUID id = UUID.randomUUID();
        when(ruleRepository.findById(Fixtures.OTHER_UNIT, id)).thenReturn(Optional.empty());
        UpdateRuleUseCases useCases = new UpdateRuleUseCases(ruleRepository, resolver());

        assertThatThrownBy(() -> useCases.execute(Fixtures.OTHER_UNIT, id, "x"))
                .isInstanceOf(RuleNotFoundException.class);
        verify(ruleRepository, never()).save(any());
    }

    // ---- alvo inexistente ou de outra unidade ----

    /**
     * Sem essa checagem o alvo invalido nao viraria relacao e a regra passaria a valer para a
     * unidade inteira, ampliando o alcance do alerta em silencio.
     */
    @Test
    void shouldRefuseToCreateWithAModelFromAnotherUnit() {
        UUID modelId = UUID.randomUUID();
        when(modelRepository.findById(Fixtures.UNIT, modelId)).thenReturn(Optional.empty());
        CreateRuleImpl useCase = new CreateRuleImpl(ruleRepository, resolver());
        CreateRuleCommand comando = new CreateRuleCommand(Fixtures.UNIT, "x", RuleKind.STALE_ITEM, 6,
                RuleLimitUnit.MONTHS, RuleTarget.model(modelId), true);

        assertThatThrownBy(() -> useCase.execute(comando))
                .isInstanceOf(com.zera.ms_inventory.core.domain.exception.ModelNotFoundException.class);
        verify(ruleRepository, never()).save(any());
    }

    @Test
    void shouldRefuseToCreateWithAnUnknownCategory() {
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findById(Fixtures.UNIT, categoryId)).thenReturn(Optional.empty());
        CreateRuleImpl useCase = new CreateRuleImpl(ruleRepository, resolver());
        CreateRuleCommand comando = new CreateRuleCommand(Fixtures.UNIT, "x", RuleKind.STALE_ITEM, 6,
                RuleLimitUnit.MONTHS, RuleTarget.category(categoryId), true);

        assertThatThrownBy(() -> useCase.execute(comando))
                .isInstanceOf(com.zera.ms_inventory.core.domain.exception.CategoryNotFoundException.class);
        verify(ruleRepository, never()).save(any());
    }

    /** Trocar um alvo valido por um invalido nao pode transformar a regra em regra da unidade. */
    @Test
    void shouldRefuseToPointTheRuleAtAModelFromAnotherUnit() {
        UUID id = UUID.randomUUID();
        UUID modelId = UUID.randomUUID();
        when(modelRepository.findById(Fixtures.UNIT, modelId)).thenReturn(Optional.empty());
        UpdateRuleUseCases useCases = new UpdateRuleUseCases(ruleRepository, resolver());

        assertThatThrownBy(() -> useCases.execute(Fixtures.UNIT, id, RuleTarget.model(modelId)))
                .isInstanceOf(com.zera.ms_inventory.core.domain.exception.ModelNotFoundException.class);
        verify(ruleRepository, never()).findById(any(), any());
        verify(ruleRepository, never()).save(any());
    }

    /** Alvo nulo e valido e nao consulta nada: e a regra voltando para a unidade inteira. */
    @Test
    void shouldNotLookUpAnythingWhenClearingTheTarget() {
        UUID id = UUID.randomUUID();
        Rule regra = rule(id);
        when(ruleRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(regra));
        when(ruleRepository.save(regra)).thenReturn(regra);

        new UpdateRuleUseCases(ruleRepository, resolver()).execute(Fixtures.UNIT, id, (RuleTarget) null);

        assertThat(regra.appliesToWholeUnit()).isTrue();
        verify(modelRepository, never()).findById(any(), any());
        verify(categoryRepository, never()).findById(any(), any());
    }
}
