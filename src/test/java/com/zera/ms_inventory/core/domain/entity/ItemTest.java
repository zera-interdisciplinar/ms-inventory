package com.zera.ms_inventory.core.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;

class ItemTest {

    private Model model(UUID unitId) {
        Category category = new Category(UUID.randomUUID(), unitId, "Notebooks", "Portable computers");
        return new Model(UUID.randomUUID(), unitId, "Notebook X", "Zera", 24, 60, Set.of("Lithium"), category);
    }

    @Test
    void shouldCreateItemWithAllFields() {
        UUID id = UUID.randomUUID();
        Barcode barcode = new Barcode("7891234567890");
        UUID unitId = UUID.randomUUID();
        Model model = model(unitId);
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 4, 12, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 8, 4, 12, 5);
        LocalDateTime lastEventAt = LocalDateTime.of(2026, 8, 4, 12, 10);
        LocalDateTime nextPredictionDate = LocalDateTime.of(2026, 8, 10, 8, 0);
        LocalDate acquiredAt = LocalDate.of(2026, 8, 4);

        Item item = new Item(id, barcode, ItemStatus.OK, unitId, model, createdAt, updatedAt, lastEventAt,
                nextPredictionDate, 2024, 7, "SN-001", acquiredAt);

        assertEquals(id, item.getId());
        assertEquals(barcode, item.getBarcode());
        assertEquals(ItemStatus.OK, item.getStatus());
        assertEquals(unitId, item.getUnitId());
        assertEquals(model, item.getModel());
        assertEquals(createdAt, item.getCreatedAt());
        assertEquals(updatedAt, item.getUpdatedAt());
        assertEquals(lastEventAt, item.getLastEventAt());
        assertEquals(nextPredictionDate, item.getNextPredictionDate());
        assertEquals(2024, item.getManufacturingDate());
        assertEquals(7, item.getUsageIntensity());
        assertEquals("SN-001", item.getSerialNumber());
        assertEquals(acquiredAt, item.getAcquiredAt());
    }

    @Test
    void shouldUpdateItemState() {
        UUID unitId = UUID.randomUUID();
        Item item = new Item(
                UUID.randomUUID(),
                new Barcode("7891234567890"),
                ItemStatus.OK,
                unitId,
                model(unitId),
                LocalDateTime.of(2026, 8, 4, 12, 10),
                LocalDateTime.of(2026, 8, 10, 8, 0),
                2024,
                7,
                "SN-001",
                LocalDate.of(2026, 8, 4)
        );

        LocalDateTime beforeUpdate = item.getUpdatedAt();

        UUID newUnitId = UUID.randomUUID();
        LocalDateTime newPredictionDate = LocalDateTime.of(2026, 8, 20, 9, 30);
        LocalDate newAcquiredAt = LocalDate.of(2026, 8, 5);

        item.updateStatus(ItemStatus.DAMAGED);
        item.assignUnit(newUnitId);
        item.updateSerialNumber("SN-002");
        item.updateAcquiredAt(newAcquiredAt);
        item.updateNextPredictionDate(newPredictionDate);
        item.updateManufacturingDate(2025);
        item.updateUsageIntensity(9);

        assertEquals(ItemStatus.DAMAGED, item.getStatus());
        assertEquals(newUnitId, item.getUnitId());
        assertEquals("SN-002", item.getSerialNumber());
        assertEquals(newAcquiredAt, item.getAcquiredAt());
        assertEquals(newPredictionDate, item.getNextPredictionDate());
        assertEquals(2025, item.getManufacturingDate());
        assertEquals(9, item.getUsageIntensity());
        assertTrue(item.getUpdatedAt().isAfter(beforeUpdate) || item.getUpdatedAt().isEqual(beforeUpdate));
    }
}
