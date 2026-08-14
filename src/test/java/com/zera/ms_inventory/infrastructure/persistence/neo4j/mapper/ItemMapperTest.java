package com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.ItemNode;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemMapperTest {

    private final ItemMapper mapper = new ItemMapper();

    @Test
    void shouldMapDomainToNodeAndBack() {
        UUID id = UUID.randomUUID();
        Item item = new Item(id, new Barcode("123456"), ItemStatus.OK, UUID.randomUUID(), LocalDateTime.now(),
                12, 5, "SN-001", LocalDate.now());

        ItemNode node = mapper.toNode(item);
        Item result = mapper.toDomain(node);

        assertEquals(item.getId(), result.getId());
        assertEquals(item.getBarcode(), result.getBarcode());
        assertEquals(item.getStatus(), result.getStatus());
        assertEquals(item.getUnitId(), result.getUnitId());
        assertEquals(item.getManufacturingDate(), result.getManufacturingDate());
        assertEquals(item.getUsageIntensity(), result.getUsageIntensity());
        assertEquals(item.getSerialNumber(), result.getSerialNumber());
        assertEquals(item.getAcquiredAt(), result.getAcquiredAt());
        assertEquals(item.getCreatedAt(), result.getCreatedAt());
        assertEquals(item.getUpdatedAt(), result.getUpdatedAt());
        assertEquals(item.getLastEventAt(), result.getLastEventAt());
        assertEquals(item.getNextPredictionDate(), result.getNextPredictionDate());
    }
}
