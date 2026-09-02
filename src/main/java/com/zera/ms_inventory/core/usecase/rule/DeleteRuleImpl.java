package com.zera.ms_inventory.core.usecase.rule;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.exception.RuleNotFoundException;
import com.zera.ms_inventory.core.repository.RuleRepository;

@Service
public class DeleteRuleImpl implements DeleteRule {
    private final RuleRepository ruleRepository;

    public DeleteRuleImpl(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Override
    public void execute(UUID id) {
        ruleRepository.findById(id)
                .orElseThrow(() -> new RuleNotFoundException(id));
        ruleRepository.deleteById(id);
    }
}
