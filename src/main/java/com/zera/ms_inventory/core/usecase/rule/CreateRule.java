package com.zera.ms_inventory.core.usecase.rule;

import com.zera.ms_inventory.core.domain.entity.Rule;

public interface CreateRule {
    Rule execute(CreateRuleCommand command);
}
