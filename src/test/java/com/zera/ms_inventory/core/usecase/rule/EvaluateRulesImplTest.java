package com.zera.ms_inventory.core.usecase.rule;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.domain.entity.RuleAlert;
import com.zera.ms_inventory.core.domain.entity.UnitInventorySettings;
import com.zera.ms_inventory.core.domain.valueobject.AlertSeverity;
import com.zera.ms_inventory.core.domain.valueobject.AlertSubject;
import com.zera.ms_inventory.core.domain.valueobject.RuleKind;
import com.zera.ms_inventory.core.domain.valueobject.RuleLimitUnit;
import com.zera.ms_inventory.core.repository.RuleEvaluationRepository;
import com.zera.ms_inventory.core.repository.RuleRepository;
import com.zera.ms_inventory.core.usecase.unit.GetUnitSettings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluateRulesImplTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 9, 26);

    @Mock private RuleRepository ruleRepository;
    @Mock private RuleEvaluationRepository evaluationRepository;
    @Mock private GetUnitSettings getUnitSettings;
    @Mock private AlertDispatcher dispatcher;

    private EvaluateRulesImpl useCase() {
        return new EvaluateRulesImpl(ruleRepository, evaluationRepository, getUnitSettings, dispatcher);
    }

    private Rule rule(RuleKind kind, Integer limite, RuleLimitUnit unidade, boolean ativa) {
        return new Rule(UUID.randomUUID(), Fixtures.UNIT, kind.name(), kind, limite, unidade, null, ativa);
    }

    private AlertSubject item(String codigo) {
        return new AlertSubject(UUID.randomUUID(), codigo, "Notebook " + codigo);
    }

    @Test
    void shouldRaiseOneAlertPerViolatingItem() {
        Rule regra = rule(RuleKind.WARRANTY_EXPIRATION, 30, RuleLimitUnit.DAYS, true);
        when(evaluationRepository.unitsWithRules()).thenReturn(List.of(Fixtures.UNIT));
        when(ruleRepository.findAll(Fixtures.UNIT)).thenReturn(List.of(regra));
        when(evaluationRepository.findViolatingItems(regra, HOJE))
                .thenReturn(List.of(item("100001"), item("100002")));
        when(dispatcher.dispatch(any())).thenReturn(AlertDispatcher.Outcome.SENT);

        RuleEvaluationResult resultado = useCase().execute(HOJE);

        assertThat(resultado.unitsVisited()).isEqualTo(1);
        assertThat(resultado.rulesEvaluated()).isEqualTo(1);
        assertThat(resultado.alertsRaised()).isEqualTo(2);
        assertThat(resultado.alertsSent()).isEqualTo(2);

        ArgumentCaptor<RuleAlert> alertas = ArgumentCaptor.forClass(RuleAlert.class);
        verify(dispatcher, org.mockito.Mockito.times(2)).dispatch(alertas.capture());
        assertThat(alertas.getAllValues()).allSatisfy(alerta -> {
            assertThat(alerta.kind()).isEqualTo(RuleKind.WARRANTY_EXPIRATION);
            assertThat(alerta.severity()).isEqualTo(AlertSeverity.MEDIUM);
            assertThat(alerta.unitId()).isEqualTo(Fixtures.UNIT);
            assertThat(alerta.description()).contains("limite: 30 days");
        });
    }

    @Test
    void shouldSkipInactiveRules() {
        Rule inativa = rule(RuleKind.STALE_ITEM, 6, RuleLimitUnit.MONTHS, false);
        when(evaluationRepository.unitsWithRules()).thenReturn(List.of(Fixtures.UNIT));
        when(ruleRepository.findAll(Fixtures.UNIT)).thenReturn(List.of(inativa));

        RuleEvaluationResult resultado = useCase().execute(HOJE);

        assertThat(resultado.rulesEvaluated()).isZero();
        verify(evaluationRepository, never()).findViolatingItems(any(), any());
    }

    /** O reciclavel no aterro e avaliado no descarte, nao no job. */
    @Test
    void shouldLeaveTheDisposalRuleToTheDisposal() {
        Rule descarte = rule(RuleKind.RECYCLABLE_TO_LANDFILL, null, null, true);
        when(evaluationRepository.unitsWithRules()).thenReturn(List.of(Fixtures.UNIT));
        when(ruleRepository.findAll(Fixtures.UNIT)).thenReturn(List.of(descarte));

        assertThat(useCase().execute(HOJE).rulesEvaluated()).isZero();
        verify(dispatcher, never()).dispatch(any());
    }

    // ---- ocupacao do estoque ----

    private UnitInventorySettings capacidade(Integer valor) {
        UnitInventorySettings settings = UnitInventorySettings.notConfigured(Fixtures.UNIT);
        if (valor != null) {
            settings.changeCapacity(valor, Fixtures.MANAGER);
        }
        return settings;
    }

    @Test
    void shouldAlertWhenOccupancyPassesTheLimit() {
        Rule regra = rule(RuleKind.STOCK_QUANTITY_LIMIT, 90, RuleLimitUnit.PERCENT, true);
        when(evaluationRepository.unitsWithRules()).thenReturn(List.of(Fixtures.UNIT));
        when(ruleRepository.findAll(Fixtures.UNIT)).thenReturn(List.of(regra));
        when(getUnitSettings.execute(Fixtures.UNIT)).thenReturn(capacidade(100));
        when(evaluationRepository.countActiveItems(Fixtures.UNIT)).thenReturn(95L);
        when(dispatcher.dispatch(any())).thenReturn(AlertDispatcher.Outcome.SENT);

        RuleEvaluationResult resultado = useCase().execute(HOJE);

        assertThat(resultado.alertsSent()).isEqualTo(1);
        ArgumentCaptor<RuleAlert> alerta = ArgumentCaptor.forClass(RuleAlert.class);
        verify(dispatcher).dispatch(alerta.capture());
        assertThat(alerta.getValue().subject().isUnitWide()).isTrue();
        assertThat(alerta.getValue().severity()).isEqualTo(AlertSeverity.HIGH);
        assertThat(alerta.getValue().description()).contains("95%").contains("95 itens de 100");
        // o assunto da regra de unidade e a propria unidade
        assertThat(alerta.getValue().dedupSubject()).isEqualTo(Fixtures.UNIT);
    }

    @Test
    void shouldStaySilentBelowTheOccupancyLimit() {
        Rule regra = rule(RuleKind.STOCK_QUANTITY_LIMIT, 90, RuleLimitUnit.PERCENT, true);
        when(evaluationRepository.unitsWithRules()).thenReturn(List.of(Fixtures.UNIT));
        when(ruleRepository.findAll(Fixtures.UNIT)).thenReturn(List.of(regra));
        when(getUnitSettings.execute(Fixtures.UNIT)).thenReturn(capacidade(100));
        when(evaluationRepository.countActiveItems(Fixtures.UNIT)).thenReturn(50L);

        assertThat(useCase().execute(HOJE).alertsRaised()).isZero();
        verify(dispatcher, never()).dispatch(any());
    }

    /** Sem capacidade configurada nao ha denominador: a regra fica inerte em vez de inventar numero. */
    @Test
    void shouldStaySilentWithoutConfiguredCapacity() {
        Rule regra = rule(RuleKind.STOCK_QUANTITY_LIMIT, 90, RuleLimitUnit.PERCENT, true);
        when(evaluationRepository.unitsWithRules()).thenReturn(List.of(Fixtures.UNIT));
        when(ruleRepository.findAll(Fixtures.UNIT)).thenReturn(List.of(regra));
        when(getUnitSettings.execute(Fixtures.UNIT)).thenReturn(capacidade(null));
        lenient().when(evaluationRepository.countActiveItems(Fixtures.UNIT)).thenReturn(500L);

        assertThat(useCase().execute(HOJE).alertsRaised()).isZero();
        verify(dispatcher, never()).dispatch(any());
    }

    @Test
    void shouldCountSuppressedAndFailedSeparately() {
        Rule regra = rule(RuleKind.PREDICTED_FAILURE, 15, RuleLimitUnit.DAYS, true);
        when(evaluationRepository.unitsWithRules()).thenReturn(List.of(Fixtures.UNIT));
        when(ruleRepository.findAll(Fixtures.UNIT)).thenReturn(List.of(regra));
        when(evaluationRepository.findViolatingItems(regra, HOJE))
                .thenReturn(List.of(item("1"), item("2"), item("3")));
        when(dispatcher.dispatch(any()))
                .thenReturn(AlertDispatcher.Outcome.SENT)
                .thenReturn(AlertDispatcher.Outcome.SUPPRESSED)
                .thenReturn(AlertDispatcher.Outcome.FAILED);

        RuleEvaluationResult resultado = useCase().execute(HOJE);

        assertThat(resultado.alertsSent()).isEqualTo(1);
        assertThat(resultado.alertsSuppressed()).isEqualTo(1);
        assertThat(resultado.alertsFailed()).isEqualTo(1);
    }

    @Test
    void shouldVisitEveryUnitWithRules() {
        Rule daPrimeira = rule(RuleKind.STALE_ITEM, 6, RuleLimitUnit.MONTHS, true);
        Rule daSegunda = new Rule(UUID.randomUUID(), Fixtures.OTHER_UNIT, "x", RuleKind.STALE_ITEM, 6,
                RuleLimitUnit.MONTHS, null, true);
        when(evaluationRepository.unitsWithRules()).thenReturn(List.of(Fixtures.UNIT, Fixtures.OTHER_UNIT));
        when(ruleRepository.findAll(Fixtures.UNIT)).thenReturn(List.of(daPrimeira));
        when(ruleRepository.findAll(Fixtures.OTHER_UNIT)).thenReturn(List.of(daSegunda));
        when(evaluationRepository.findViolatingItems(any(), any())).thenReturn(List.of());

        RuleEvaluationResult resultado = useCase().execute(HOJE);

        assertThat(resultado.unitsVisited()).isEqualTo(2);
        assertThat(resultado.rulesEvaluated()).isEqualTo(2);
    }

    @Test
    void shouldUseTodayWithoutAReference() {
        when(evaluationRepository.unitsWithRules()).thenReturn(List.of());

        assertThat(useCase().execute(null)).isEqualTo(RuleEvaluationResult.empty());
    }
}
