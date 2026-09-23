package com.zera.ms_inventory.infrastructure.http.request;

import java.util.UUID;

import jakarta.validation.constraints.AssertTrue;

import com.zera.ms_inventory.core.domain.valueobject.RuleTarget;
import com.zera.ms_inventory.core.domain.valueobject.RuleTargetType;

/** Corpo vazio devolve a regra para a unidade inteira. */
public record UpdateRuleTargetRequest(RuleTargetType targetType, UUID targetId) {

    @AssertTrue(message = "inform both targetType and targetId, or neither")
    public boolean isTargetComplete() {
        return (targetType == null) == (targetId == null);
    }

    public RuleTarget toTarget() {
        return RuleTarget.of(targetType, targetId);
    }
}
