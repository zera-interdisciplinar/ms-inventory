package com.zera.ms_inventory.core.usecase.rule;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Rule;

public interface FindRuleById {
    Rule execute(UUID id);
}
