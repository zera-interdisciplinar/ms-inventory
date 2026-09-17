package com.zera.ms_inventory.infrastructure.http.response;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.domain.valueobject.RuleKind;
import com.zera.ms_inventory.core.domain.valueobject.RuleLimitUnit;
import com.zera.ms_inventory.core.domain.valueobject.RuleTargetType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResponseMappingTest {

    @Test
    void shouldMapItemWithNestedModelAndCategory() {
        Item item = Fixtures.item(Fixtures.UNIT);

        ItemResponse response = ItemResponse.from(item, null);

        assertEquals(item.getId(), response.id());
        assertEquals("7891234567890", response.barcode());
        assertEquals(item.getModel().getId(), response.model().id());
        assertEquals(item.getModel().getCategory().getName(), response.model().category().name());
        assertEquals(item.getSerialNumber(), response.serialNumber());
    }

    @Test
    void shouldMapRule() {
        UUID targetId = UUID.randomUUID();
        Rule rule = new Rule(UUID.randomUUID(), "Garantia", RuleKind.WARRANTY_EXPIRATION, 30,
                RuleLimitUnit.DAYS, RuleTargetType.MODEL, targetId, true);

        RuleResponse response = RuleResponse.from(rule);

        assertEquals(RuleKind.WARRANTY_EXPIRATION, response.kind());
        assertEquals(targetId, response.targetId());
        assertTrue(response.active());
    }

    @Test
    void shouldReturnNullForMissingDomainObjects() {
        assertNull(CategoryResponse.from(null));
        assertNull(ModelResponse.from(null));
        assertNull(ItemResponse.from(null, null));
        assertNull(RuleResponse.from(null));
    }
}
