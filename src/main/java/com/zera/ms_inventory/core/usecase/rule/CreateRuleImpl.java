package com.zera.ms_inventory.core.usecase.rule;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.domain.valueobject.RuleKind;
import com.zera.ms_inventory.core.domain.valueobject.RuleLimitUnit;
import com.zera.ms_inventory.core.domain.valueobject.RuleTargetType;
import com.zera.ms_inventory.core.repository.RuleRepository;

@Service
public class CreateRuleImpl implements CreateRule {
    private final RuleRepository ruleRepository;

    public CreateRuleImpl(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Override
    public Rule execute(String name, RuleKind kind, Integer limitValue, RuleLimitUnit limitUnit,
                         RuleTargetType targetType, UUID targetId, boolean active, LocalDateTime createdAt,
                         LocalDateTime updatedAt) {
        Rule rule = new Rule(UUID.randomUUID(), name, kind, limitValue, limitUnit, targetType, targetId, active,
                createdAt, updatedAt);
        return ruleRepository.save(rule);
    }
}
