package com.zera.ms_inventory.core.usecase.rule;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.valueobject.RuleKind;
import com.zera.ms_inventory.core.domain.valueobject.RuleLimitUnit;
import com.zera.ms_inventory.core.domain.valueobject.RuleTarget;

/** {@code target} nulo faz a regra valer para a unidade inteira. */
public record CreateRuleCommand(UUID unitId, String name, RuleKind kind, Integer limitValue,
                                RuleLimitUnit limitUnit, RuleTarget target, boolean active) {}
