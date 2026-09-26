package com.zera.ms_inventory.core.usecase.rule;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.domain.entity.RuleAlert;
import com.zera.ms_inventory.core.domain.entity.UnitInventorySettings;
import com.zera.ms_inventory.core.domain.valueobject.AlertSeverity;
import com.zera.ms_inventory.core.domain.valueobject.AlertSubject;
import com.zera.ms_inventory.core.domain.valueobject.RuleKind;
import com.zera.ms_inventory.core.repository.RuleEvaluationRepository;
import com.zera.ms_inventory.core.repository.RuleRepository;
import com.zera.ms_inventory.core.usecase.unit.GetUnitSettings;

@Service
public class EvaluateRulesImpl implements EvaluateRules {

    private static final Logger log = LoggerFactory.getLogger(EvaluateRulesImpl.class);

    private final RuleRepository ruleRepository;
    private final RuleEvaluationRepository evaluationRepository;
    private final GetUnitSettings getUnitSettings;
    private final AlertDispatcher dispatcher;

    public EvaluateRulesImpl(RuleRepository ruleRepository, RuleEvaluationRepository evaluationRepository,
                             GetUnitSettings getUnitSettings, AlertDispatcher dispatcher) {
        this.ruleRepository = ruleRepository;
        this.evaluationRepository = evaluationRepository;
        this.getUnitSettings = getUnitSettings;
        this.dispatcher = dispatcher;
    }

    @Override
    public RuleEvaluationResult execute(LocalDate reference) {
        LocalDate hoje = reference != null ? reference : LocalDate.now();
        int unidades = 0;
        int regras = 0;
        int levantados = 0;
        int enviados = 0;
        int suprimidos = 0;
        int falhos = 0;

        for (UUID unitId : evaluationRepository.unitsWithRules()) {
            unidades++;
            for (Rule rule : ruleRepository.findAll(unitId)) {
                if (!rule.isActive() || rule.getKind() == RuleKind.RECYCLABLE_TO_LANDFILL) {
                    // o reciclavel no aterro e avaliado no descarte, nao aqui
                    continue;
                }
                regras++;
                for (RuleAlert alert : alertsFor(rule, hoje)) {
                    levantados++;
                    switch (dispatcher.dispatch(alert)) {
                        case SENT -> enviados++;
                        case SUPPRESSED -> suprimidos++;
                        case FAILED -> falhos++;
                    }
                }
            }
        }

        RuleEvaluationResult resultado = new RuleEvaluationResult(unidades, regras, levantados, enviados,
                suprimidos, falhos);
        log.info("Rule evaluation finished: {}", resultado);
        return resultado;
    }

    private List<RuleAlert> alertsFor(Rule rule, LocalDate reference) {
        if (rule.getKind() == RuleKind.STOCK_QUANTITY_LIMIT) {
            return stockAlert(rule).map(List::of).orElseGet(List::of);
        }
        return evaluationRepository.findViolatingItems(rule, reference).stream()
                .map(subject -> alert(rule, subject, describe(rule, subject)))
                .toList();
    }

    /**
     * Ocupacao e da unidade: sem capacidade configurada nao ha denominador, entao a regra fica
     * inerte em vez de alertar com numero inventado.
     */
    private java.util.Optional<RuleAlert> stockAlert(Rule rule) {
        UnitInventorySettings settings = getUnitSettings.execute(rule.getUnitId());
        long itens = evaluationRepository.countActiveItems(rule.getUnitId());
        java.util.OptionalDouble ocupacao = settings.occupancyPercent(itens);
        if (ocupacao.isEmpty() || rule.getLimitValue() == null
                || ocupacao.getAsDouble() < rule.getLimitValue()) {
            return java.util.Optional.empty();
        }
        String descricao = "Ocupacao do estoque em %.0f%%, acima do limite de %d%% (%d itens de %d)"
                .formatted(ocupacao.getAsDouble(), rule.getLimitValue(), itens, settings.getStockCapacity());
        return java.util.Optional.of(alert(rule, AlertSubject.unit(), descricao));
    }

    private RuleAlert alert(Rule rule, AlertSubject subject, String description) {
        return new RuleAlert(rule.getUnitId(), rule.getId(), rule.getKind(), subject,
                severityOf(rule.getKind()), description, java.time.LocalDateTime.now());
    }

    private static String describe(Rule rule, AlertSubject subject) {
        String limite = rule.getLimitValue() == null ? ""
                : " (limite: %d %s)".formatted(rule.getLimitValue(),
                        rule.getLimitUnit() == null ? "" : rule.getLimitUnit().name().toLowerCase());
        return "%s: %s%s".formatted(rule.getName() != null ? rule.getName() : rule.getKind().name(),
                subject.describe(), limite);
    }

    /** Gravidade fixa por tipo: o que ameaca perda de valor ou seguranca entra como alta. */
    private static AlertSeverity severityOf(RuleKind kind) {
        return switch (kind) {
            case PREDICTED_FAILURE, RECYCLABLE_TO_LANDFILL, STOCK_QUANTITY_LIMIT -> AlertSeverity.HIGH;
            case WARRANTY_EXPIRATION, LIFESPAN_EXPIRATION -> AlertSeverity.MEDIUM;
            case USAGE_INTENSITY_LIMIT, TIME_IN_STOCK_LIMIT, STALE_ITEM -> AlertSeverity.LOW;
        };
    }
}
