package com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.ActorRole;
import com.zera.ms_inventory.core.domain.valueobject.DamageType;
import com.zera.ms_inventory.core.domain.valueobject.ItemCondition;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.ItemNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ItemMapperTest {

    private final ModelMapper modelMapper = new ModelMapper(new CategoryMapper());
    private final ItemMapper mapper = new ItemMapper(modelMapper);

    @Test
    void shouldMapDomainToNodeAndBack() {
        UUID id = UUID.randomUUID();
        Item item = Fixtures.item(id, Fixtures.UNIT);

        ItemNode node = mapper.toNode(item);
        Item result = mapper.toDomain(node);

        assertEquals(item.getId(), result.getId());
        assertEquals(Fixtures.UNIT, result.getUnitId());
        assertEquals(item.getBarcode().getValue(), result.getBarcode().getValue());
        assertEquals(item.getStatus(), result.getStatus());
        assertEquals(item.getSerialNumber(), result.getSerialNumber());
        assertEquals(item.getAcquiredAt(), result.getAcquiredAt());
        assertEquals(item.getManufacturingYear(), result.getManufacturingYear());
        assertEquals(item.getPredictedFailureDate(), result.getPredictedFailureDate());
        assertEquals(item.getUsageIntensity(), result.getUsageIntensity());
    }

    @Test
    void shouldLeaveModelToThePersistenceLayer() {
        ItemNode node = mapper.toNode(Fixtures.item(Fixtures.UNIT));

        assertNull(node.getModel());
    }

    @Test
    void shouldMapModelBackWhenTheNodeHasOne() {
        ItemNode node = mapper.toNode(Fixtures.item(Fixtures.UNIT));
        node.setModel(modelMapper.toNode(Fixtures.model(Fixtures.UNIT)));

        assertNotNull(mapper.toDomain(node).getModel());
    }

    @Test
    void shouldMapNullsToNull() {
        assertNull(mapper.toNode(null));
        assertNull(mapper.toDomain(null));
    }

    @Test
    void shouldMapRegistrationDataBothWays() {
        UUID operator = UUID.randomUUID();
        Item item = Fixtures.item(Fixtures.UNIT);
        item.describe("Placa de vídeo", ItemCondition.DAMAGED, true, java.util.Set.of(DamageType.DOES_NOT_POWER_ON),
                "Não liga");
        item.registerBy(new Actor(operator, ActorRole.EMPLOYEE, "Gustavo Macal"));

        Item result = mapper.toDomain(mapper.toNode(item));

        assertEquals("Placa de vídeo", result.getName());
        assertEquals(ItemCondition.DAMAGED, result.getCondition());
        assertEquals(Boolean.TRUE, result.getHasDamages());
        assertEquals(java.util.Set.of(DamageType.DOES_NOT_POWER_ON), result.getDamages());
        assertEquals("Não liga", result.getNotes());
        assertEquals(operator, result.getCreatedBy());
        assertEquals("Gustavo Macal", result.getCreatedByName());
        assertEquals(item.getUpdatedAt(), result.getUpdatedAt());
    }
}
