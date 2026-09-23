package com.zera.ms_inventory.infrastructure.http.response;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Event;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.domain.valueobject.EventType;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.domain.valueobject.RuleKind;
import com.zera.ms_inventory.core.domain.valueobject.RuleLimitUnit;
import com.zera.ms_inventory.core.domain.valueobject.RuleTarget;
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
        Rule rule = new Rule(UUID.randomUUID(), Fixtures.UNIT, "Garantia", RuleKind.WARRANTY_EXPIRATION, 30,
                RuleLimitUnit.DAYS, RuleTarget.model(targetId), true);

        RuleResponse response = RuleResponse.from(rule);

        assertEquals(RuleKind.WARRANTY_EXPIRATION, response.kind());
        assertEquals(RuleTargetType.MODEL, response.targetType());
        assertEquals(targetId, response.targetId());
        assertEquals(Fixtures.UNIT, response.unitId());
        assertTrue(response.active());
        assertTrue(!response.appliesToWholeUnit());
    }

    /** Regra sem alvo vale para a unidade inteira, e a resposta diz isso explicitamente. */
    @Test
    void shouldMapAWholeUnitRule() {
        Rule rule = new Rule(UUID.randomUUID(), Fixtures.UNIT, "Estoque cheio",
                RuleKind.STOCK_QUANTITY_LIMIT, 90, RuleLimitUnit.PERCENT, null, true);

        RuleResponse response = RuleResponse.from(rule);

        assertNull(response.targetType());
        assertNull(response.targetId());
        assertTrue(response.appliesToWholeUnit());
        assertEquals(RuleLimitUnit.PERCENT, response.limitUnit());
    }

    @Test
    void shouldMapTheEventWithActorAndReason() {
        UUID itemId = UUID.randomUUID();
        Event event = Event.of(itemId, Fixtures.UNIT, EventType.REJECTED, ItemStatus.PENDING_APPROVAL,
                ItemStatus.REJECTED, "Foto ilegivel", Fixtures.MANAGER);

        EventResponse response = EventResponse.from(event);

        assertEquals(itemId, response.itemId());
        assertEquals(EventType.REJECTED, response.type());
        assertEquals(ItemStatus.PENDING_APPROVAL, response.fromStatus());
        assertEquals(ItemStatus.REJECTED, response.toStatus());
        assertEquals("Foto ilegivel", response.reason());
        assertEquals("Kevin Gestor", response.actorName());
    }

    @Test
    void shouldReturnNullForMissingDomainObjects() {
        assertNull(CategoryResponse.from(null));
        assertNull(ModelResponse.from(null));
        assertNull(ItemResponse.from(null, null));
        assertNull(RuleResponse.from(null));
        assertNull(EventResponse.from(null));
    }
}
