package com.zera.ms_inventory.core.usecase.rule;

import java.time.LocalDateTime;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.domain.valueobject.RuleKind;
import com.zera.ms_inventory.core.domain.valueobject.RuleLimitUnit;
import com.zera.ms_inventory.core.domain.valueobject.RuleTargetType;

public interface CreateRule {
    Rule execute(String name, RuleKind kind, Integer limitValue, RuleLimitUnit limitUnit, RuleTargetType targetType,
                 UUID targetId, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt);
}
