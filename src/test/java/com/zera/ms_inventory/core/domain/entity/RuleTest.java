package com.zera.ms_inventory.core.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.zera.ms_inventory.core.domain.valueobject.RuleKind;
import com.zera.ms_inventory.core.domain.valueobject.RuleLimitUnit;
import com.zera.ms_inventory.core.domain.valueobject.RuleTargetType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleTest {

    @Test
    void shouldCreateRuleWithAllFields() {
        UUID id = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 4, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 8, 4, 10, 5);

        Rule rule = new Rule(id, "Warranty check", RuleKind.WARRANTY_EXPIRATION, 12, RuleLimitUnit.MONTHS,
                RuleTargetType.MODEL, targetId, true, createdAt, updatedAt);

        assertEquals(id, rule.getId());
        assertEquals("Warranty check", rule.getName());
        assertEquals(RuleKind.WARRANTY_EXPIRATION, rule.getKind());
        assertEquals(12, rule.getLimitValue());
        assertEquals(RuleLimitUnit.MONTHS, rule.getLimitUnit());
        assertEquals(RuleTargetType.MODEL, rule.getTargetType());
        assertEquals(targetId, rule.getTargetId());
        assertTrue(rule.isActive());
        assertEquals(createdAt, rule.getCreatedAt());
        assertEquals(updatedAt, rule.getUpdatedAt());
    }

    @Test
    void shouldRenameChangeLimitAndTarget() {
        Rule rule = new Rule(UUID.randomUUID(), "Warranty check", RuleKind.WARRANTY_EXPIRATION, 12,
                RuleLimitUnit.MONTHS, RuleTargetType.MODEL, UUID.randomUUID(), true);
        LocalDateTime beforeUpdate = rule.getUpdatedAt();
        UUID newTargetId = UUID.randomUUID();

        rule.rename("Lifespan check");
        rule.changeLimit(24, RuleLimitUnit.MONTHS);
        rule.changeTarget(RuleTargetType.CATEGORY, newTargetId);

        assertEquals("Lifespan check", rule.getName());
        assertEquals(24, rule.getLimitValue());
        assertEquals(RuleTargetType.CATEGORY, rule.getTargetType());
        assertEquals(newTargetId, rule.getTargetId());
        assertTrue(rule.getUpdatedAt().isAfter(beforeUpdate) || rule.getUpdatedAt().isEqual(beforeUpdate));
    }

    @Test
    void shouldActivateAndDeactivate() {
        Rule rule = new Rule(UUID.randomUUID(), "Warranty check", RuleKind.WARRANTY_EXPIRATION, 12,
                RuleLimitUnit.MONTHS, RuleTargetType.MODEL, UUID.randomUUID(), false);

        rule.activate();
        assertTrue(rule.isActive());

        rule.deactivate();
        assertFalse(rule.isActive());
    }
}
