package com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper;

import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.domain.valueobject.RuleKind;
import com.zera.ms_inventory.core.domain.valueobject.RuleLimitUnit;
import com.zera.ms_inventory.core.domain.valueobject.RuleTargetType;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.RuleNode;

@Component
public class RuleMapper {

    public Rule toDomain(RuleNode node) {
        return new Rule(node.getId(), node.getName(), RuleKind.valueOf(node.getKind()), node.getLimitValue(),
                RuleLimitUnit.valueOf(node.getLimitUnit()), RuleTargetType.valueOf(node.getTargetType()),
                node.getTargetId(), node.isActive(), node.getCreatedAt(), node.getUpdatedAt());
    }

    public RuleNode toNode(Rule rule) {
        return new RuleNode(rule.getId(), rule.getName(), rule.getKind().name(), rule.getLimitValue(),
                rule.getLimitUnit().name(), rule.getTargetType().name(), rule.getTargetId(), rule.isActive(),
                rule.getCreatedAt(), rule.getUpdatedAt());
    }
}
