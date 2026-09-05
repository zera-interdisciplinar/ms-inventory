package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.usecase.item.FindAllItems;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryHealthToolTest {

    @Mock
    private FindAllItems findAllItems;

    private Item item(ItemStatus status, String serialNumber) {
        return new Item(UUID.randomUUID(), new Barcode("123456"), status, Fixtures.UNIT,
                Fixtures.model(Fixtures.UNIT), LocalDateTime.now(), 2024, 7, serialNumber, LocalDate.now());
    }

    @Test
    void shouldReturnZeroedReportWhenTheUnitHasNoItems() {
        when(findAllItems.execute(Fixtures.UNIT)).thenReturn(List.of());

        var report = new InventoryHealthTool(findAllItems).getInventoryHealth(Fixtures.UNIT);

        assertEquals(0, report.totalItems);
        assertEquals(0.0, report.healthScore);
    }

    @Test
    void shouldScoreTheUnitInventory() {
        when(findAllItems.execute(Fixtures.UNIT)).thenReturn(List.of(
                item(ItemStatus.OK, "SN-001"),
                item(ItemStatus.DAMAGED, "SN-002"),
                item(ItemStatus.OK, null)));

        var report = new InventoryHealthTool(findAllItems).getInventoryHealth(Fixtures.UNIT);

        assertEquals(3, report.totalItems);
        assertEquals(1, report.damagedItems);
        assertEquals(2, report.okItems);
        assertEquals(1, report.itemsWithoutSerialNumber);
        assertEquals(0, report.itemsWithoutUnitAssignment);
        assertEquals(200.0 / 3, report.healthScore, 0.0001);
    }

    @Test
    void shouldRejectMissingUnitIdInsteadOfFallingBackToAGlobalRead() {
        InventoryHealthTool tool = new InventoryHealthTool(findAllItems);

        assertThrows(IllegalArgumentException.class, () -> tool.getInventoryHealth(null));
        verifyNoInteractions(findAllItems);
    }
}
