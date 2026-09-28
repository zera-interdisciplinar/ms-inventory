package com.zera.ms_inventory.infrastructure.http.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.domain.valueobject.RuleKind;
import com.zera.ms_inventory.core.domain.valueobject.RuleLimitUnit;
import com.zera.ms_inventory.core.domain.valueobject.RuleTargetType;

/** {@code targetType} e {@code targetId} nulos significam regra da unidade inteira. */
public record RuleResponse(
        UUID id,
        UUID unitId,
        String name,
        RuleKind kind,
        Integer limitValue,
        RuleLimitUnit limitUnit,
        RuleTargetType targetType,
        UUID targetId,
        boolean appliesToWholeUnit,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static RuleResponse from(Rule rule) {
        if (rule == null) {
            return null;
        }
        return new RuleResponse(rule.getId(), rule.getUnitId(), rule.getName(), rule.getKind(),
                rule.getLimitValue(), rule.getLimitUnit(),
                rule.getTarget() == null ? null : rule.getTarget().type(),
                rule.getTarget() == null ? null : rule.getTarget().id(),
                rule.appliesToWholeUnit(), rule.isActive(), rule.getCreatedAt(), rule.getUpdatedAt());
    }
}
