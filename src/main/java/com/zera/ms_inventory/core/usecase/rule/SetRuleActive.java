package com.zera.ms_inventory.core.usecase.rule;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Rule;

public interface SetRuleActive {
    Rule execute(UUID unitId, UUID id, boolean active);
}
