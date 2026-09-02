package com.zera.ms_inventory.core.usecase.rule;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.domain.exception.RuleNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.RuleLimitUnit;
import com.zera.ms_inventory.core.repository.RuleRepository;

@Service
public class UpdateRuleLimitImpl implements UpdateRuleLimit {
    private final RuleRepository ruleRepository;

    public UpdateRuleLimitImpl(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Override
    public Rule execute(UUID id, Integer limitValue, RuleLimitUnit limitUnit) {
        Rule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new RuleNotFoundException(id));
        rule.changeLimit(limitValue, limitUnit);
        return ruleRepository.save(rule);
    }
}
