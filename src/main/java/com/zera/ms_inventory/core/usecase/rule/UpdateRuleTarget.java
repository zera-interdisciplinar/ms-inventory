package com.zera.ms_inventory.core.usecase.rule;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.domain.valueobject.RuleTarget;

public interface UpdateRuleTarget {
    /** {@code target} nulo devolve a regra para a unidade inteira. */
    Rule execute(UUID unitId, UUID id, RuleTarget target);
}
