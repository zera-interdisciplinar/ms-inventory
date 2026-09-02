package com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.domain.valueobject.RuleKind;
import com.zera.ms_inventory.core.domain.valueobject.RuleLimitUnit;
import com.zera.ms_inventory.core.domain.valueobject.RuleTargetType;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.RuleNode;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RuleMapperTest {

    private final RuleMapper mapper = new RuleMapper();

    @Test
    void shouldMapDomainToNodeAndBack() {
        UUID id = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        Rule rule = new Rule(id, "Warranty check", RuleKind.WARRANTY_EXPIRATION, 12, RuleLimitUnit.MONTHS,
                RuleTargetType.MODEL, targetId, true);

        RuleNode node = mapper.toNode(rule);
        Rule result = mapper.toDomain(node);

        assertEquals(rule.getId(), result.getId());
        assertEquals(rule.getName(), result.getName());
        assertEquals(rule.getKind(), result.getKind());
        assertEquals(rule.getLimitValue(), result.getLimitValue());
        assertEquals(rule.getLimitUnit(), result.getLimitUnit());
        assertEquals(rule.getTargetType(), result.getTargetType());
        assertEquals(rule.getTargetId(), result.getTargetId());
        assertEquals(rule.isActive(), result.isActive());
        assertEquals(rule.getCreatedAt(), result.getCreatedAt());
        assertEquals(rule.getUpdatedAt(), result.getUpdatedAt());
    }
}
