package com.zera.ms_inventory.infrastructure.http.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.PositiveOrZero;

import com.zera.ms_inventory.core.domain.valueobject.RuleLimitUnit;

/**
 * Limite opcional: há regra que só liga e desliga. Mas valor e unidade andam juntos — número sem
 * unidade não dá para interpretar, e unidade sem número não limita nada.
 */
public record UpdateRuleLimitRequest(@PositiveOrZero Integer limitValue, RuleLimitUnit limitUnit) {

    @AssertTrue(message = "inform both limitValue and limitUnit, or neither")
    public boolean isLimitComplete() {
        return (limitValue == null) == (limitUnit == null);
    }
}
