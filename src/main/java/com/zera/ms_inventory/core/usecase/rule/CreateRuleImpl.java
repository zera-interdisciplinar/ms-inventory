package com.zera.ms_inventory.core.usecase.rule;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.repository.RuleRepository;

@Service
public class CreateRuleImpl implements CreateRule {

    private final RuleRepository ruleRepository;
    private final RuleTargetResolver targetResolver;

    public CreateRuleImpl(RuleRepository ruleRepository, RuleTargetResolver targetResolver) {
        this.ruleRepository = ruleRepository;
        this.targetResolver = targetResolver;
    }

    @Override
    public Rule execute(CreateRuleCommand command) {
        targetResolver.requireExists(command.unitId(), command.target());
        return ruleRepository.save(new Rule(UUID.randomUUID(), command.unitId(), command.name(),
                command.kind(), command.limitValue(), command.limitUnit(), command.target(),
                command.active()));
    }
}
