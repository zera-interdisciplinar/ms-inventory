package com.zera.ms_inventory.infrastructure.http.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.domain.valueobject.RuleKind;
import com.zera.ms_inventory.core.domain.valueobject.RuleLimitUnit;
import com.zera.ms_inventory.core.domain.valueobject.RuleTargetType;

public record RuleResponse(
        UUID id,
        String name,
        RuleKind kind,
        Integer limitValue,
        RuleLimitUnit limitUnit,
        RuleTargetType targetType,
        UUID targetId,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static RuleResponse from(Rule rule) {
        if (rule == null) {
            return null;
        }
        return new RuleResponse(rule.getId(), rule.getName(), rule.getKind(), rule.getLimitValue(),
                rule.getLimitUnit(), rule.getTargetType(), rule.getTargetId(), rule.isActive(),
                rule.getCreatedAt(), rule.getUpdatedAt());
    }
}
