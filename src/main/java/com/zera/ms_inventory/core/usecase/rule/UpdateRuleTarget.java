package com.zera.ms_inventory.core.usecase.rule;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.domain.valueobject.RuleTargetType;

public interface UpdateRuleTarget {
    Rule execute(UUID id, RuleTargetType targetType, UUID targetId);
}
