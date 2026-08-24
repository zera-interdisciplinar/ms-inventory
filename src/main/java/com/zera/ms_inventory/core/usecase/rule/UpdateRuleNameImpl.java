package com.zera.ms_inventory.core.usecase.rule;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.domain.exception.RuleNotFoundException;
import com.zera.ms_inventory.core.repository.RuleRepository;

@Service
public class UpdateRuleNameImpl implements UpdateRuleName {
    private final RuleRepository ruleRepository;

    public UpdateRuleNameImpl(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Override
    public Rule execute(UUID id, String name) {
        Rule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new RuleNotFoundException(id));
        rule.rename(name);
        return ruleRepository.save(rule);
    }
}
