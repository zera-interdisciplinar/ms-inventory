package com.zera.ms_inventory.core.usecase.rule;

import java.util.List;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.repository.RuleRepository;

@Service
public class FindAllRulesImpl implements FindAllRules {
    private final RuleRepository ruleRepository;

    public FindAllRulesImpl(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Override
    public List<Rule> execute() {
        return ruleRepository.findAll();
    }
}
