package com.zera.ms_inventory.core.usecase.rule;

import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.domain.exception.RuleNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.RuleLimitUnit;
import com.zera.ms_inventory.core.domain.valueobject.RuleTarget;
import com.zera.ms_inventory.core.repository.RuleRepository;

/** Edicoes da regra; todas resolvem a regra pelo par (unidade, id) antes de mexer. */
@Service
public class UpdateRuleUseCases implements UpdateRuleName, UpdateRuleLimit, UpdateRuleTarget, SetRuleActive {

    private final RuleRepository ruleRepository;
    private final RuleTargetResolver targetResolver;

    public UpdateRuleUseCases(RuleRepository ruleRepository, RuleTargetResolver targetResolver) {
        this.ruleRepository = ruleRepository;
        this.targetResolver = targetResolver;
    }

    @Override
    public Rule execute(UUID unitId, UUID id, String name) {
        return change(unitId, id, rule -> rule.rename(name));
    }

    @Override
    public Rule execute(UUID unitId, UUID id, Integer limitValue, RuleLimitUnit limitUnit) {
        return change(unitId, id, rule -> rule.changeLimit(limitValue, limitUnit));
    }

    @Override
    public Rule execute(UUID unitId, UUID id, RuleTarget target) {
        targetResolver.requireExists(unitId, target);
        return change(unitId, id, rule -> rule.changeTarget(target));
    }

    @Override
    public Rule execute(UUID unitId, UUID id, boolean active) {
        return change(unitId, id, rule -> {
            if (active) {
                rule.activate();
            } else {
                rule.deactivate();
            }
        });
    }

    @Transactional
    private Rule change(UUID unitId, UUID id, Consumer<Rule> mudanca) {
        Rule rule = ruleRepository.findById(unitId, id)
                .orElseThrow(() -> new RuleNotFoundException(id));
        mudanca.accept(rule);
        return ruleRepository.save(rule);
    }
}
