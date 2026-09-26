package com.zera.ms_inventory.core.domain.valueobject;

import java.time.LocalDate;

/**
 * Converte o limite da regra numa data de corte. Regras de vencimento olham para frente ("vence
 * nos proximos N dias"); regras de permanencia olham para tras ("esta ha mais de N meses assim").
 */
public final class RuleWindow {

    private RuleWindow() {
    }

    /** Data limite adiante; nulo quando a regra nao tem limite em tempo. */
    public static LocalDate forward(LocalDate reference, Integer limitValue, RuleLimitUnit limitUnit) {
        return shift(reference, limitValue, limitUnit, true);
    }

    /** Data limite para tras; nulo quando a regra nao tem limite em tempo. */
    public static LocalDate backward(LocalDate reference, Integer limitValue, RuleLimitUnit limitUnit) {
        return shift(reference, limitValue, limitUnit, false);
    }

    private static LocalDate shift(LocalDate reference, Integer limitValue, RuleLimitUnit limitUnit,
                                   boolean ahead) {
        if (reference == null || limitValue == null || limitUnit == null) {
            return null;
        }
        long amount = ahead ? limitValue : -limitValue;
        return switch (limitUnit) {
            case DAYS -> reference.plusDays(amount);
            case MONTHS -> reference.plusMonths(amount);
            // UNITS e PERCENT nao sao janelas de tempo
            case UNITS, PERCENT -> null;
        };
    }
}
