package com.zera.ms_inventory.core.usecase.rule;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.domain.valueobject.RuleLimitUnit;

public interface UpdateRuleLimit {
    Rule execute(UUID id, Integer limitValue, RuleLimitUnit limitUnit);
}
