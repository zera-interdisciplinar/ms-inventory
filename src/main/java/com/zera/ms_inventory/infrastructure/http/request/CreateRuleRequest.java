package com.zera.ms_inventory.infrastructure.http.request;

import java.util.UUID;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import com.zera.ms_inventory.core.domain.valueobject.RuleKind;
import com.zera.ms_inventory.core.domain.valueobject.RuleLimitUnit;
import com.zera.ms_inventory.core.domain.valueobject.RuleTarget;
import com.zera.ms_inventory.core.domain.valueobject.RuleTargetType;
import com.zera.ms_inventory.core.usecase.rule.CreateRuleCommand;

/**
 * Alvo opcional: sem {@code targetType} e {@code targetId} a regra vale para a unidade inteira.
 * O limite também é opcional, porque há regra que só liga e desliga.
 */
public record CreateRuleRequest(
        @NotBlank String name,
        @NotNull RuleKind kind,
        @PositiveOrZero Integer limitValue,
        RuleLimitUnit limitUnit,
        RuleTargetType targetType,
        UUID targetId,
        boolean active
) {
    @AssertTrue(message = "inform both targetType and targetId, or neither")
    public boolean isTargetComplete() {
        return (targetType == null) == (targetId == null);
    }

    @AssertTrue(message = "inform both limitValue and limitUnit, or neither")
    public boolean isLimitComplete() {
        return (limitValue == null) == (limitUnit == null);
    }

    public CreateRuleCommand toCommand(UUID unitId) {
        return new CreateRuleCommand(unitId, name, kind, limitValue, limitUnit,
                RuleTarget.of(targetType, targetId), active);
    }
}
