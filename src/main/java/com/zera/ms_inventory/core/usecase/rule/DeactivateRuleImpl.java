package com.zera.ms_inventory.core.usecase.rule;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.domain.exception.RuleNotFoundException;
import com.zera.ms_inventory.core.repository.RuleRepository;

@Service
public class DeactivateRuleImpl implements DeactivateRule {
    private final RuleRepository ruleRepository;

    public DeactivateRuleImpl(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Override
    public Rule execute(UUID id) {
        Rule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new RuleNotFoundException(id));
        rule.deactivate();
        return ruleRepository.save(rule);
    }
}
