package com.zera.ms_inventory.core.usecase.rule;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Rule;

public interface DeactivateRule {
    Rule execute(UUID id);
}
