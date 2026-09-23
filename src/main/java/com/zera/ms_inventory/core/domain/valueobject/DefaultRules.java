package com.zera.ms_inventory.core.domain.valueobject;

import java.util.List;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Rule;

/**
 * Regras que toda unidade ganha no primeiro acesso, para o alerta funcionar sem ninguem configurar
 * nada. Todas valem para a unidade inteira e podem ser editadas ou desligadas depois.
 */
public final class DefaultRules {

    private DefaultRules() {
    }

    public static List<Rule> forUnit(UUID unitId) {
        return List.of(
                rule(unitId, "Garantia vencendo", RuleKind.WARRANTY_EXPIRATION, 30, RuleLimitUnit.DAYS),
                rule(unitId, "Vida util no fim", RuleKind.LIFESPAN_EXPIRATION, 60, RuleLimitUnit.DAYS),
                rule(unitId, "Intensidade de uso alta", RuleKind.USAGE_INTENSITY_LIMIT, 8, RuleLimitUnit.UNITS),
                rule(unitId, "Estoque cheio", RuleKind.STOCK_QUANTITY_LIMIT, 90, RuleLimitUnit.PERCENT),
                rule(unitId, "Muito tempo em estoque", RuleKind.TIME_IN_STOCK_LIMIT, 12, RuleLimitUnit.MONTHS),
                rule(unitId, "Item parado", RuleKind.STALE_ITEM, 6, RuleLimitUnit.MONTHS),
                rule(unitId, "Reciclavel indo para o aterro", RuleKind.RECYCLABLE_TO_LANDFILL, null, null),
                rule(unitId, "Quebra prevista", RuleKind.PREDICTED_FAILURE, 15, RuleLimitUnit.DAYS));
    }

    private static Rule rule(UUID unitId, String name, RuleKind kind, Integer limitValue,
                             RuleLimitUnit limitUnit) {
        return new Rule(UUID.randomUUID(), unitId, name, kind, limitValue, limitUnit, null, true);
    }
}
