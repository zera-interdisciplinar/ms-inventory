package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.usecase.item.FindAllItems;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryHealthToolTest {

    @Mock
    private FindAllItems findAllItems;

    @Test
    void shouldReturnEmptyReportWhenNoItems() {
        when(findAllItems.execute()).thenReturn(List.of());

        InventoryHealthTool tool = new InventoryHealthTool(findAllItems);

        InventoryHealthTool.InventoryHealthReport report = tool.getInventoryHealth(null, null);

        assertEquals(0, report.totalItems);
        assertEquals(0.0, report.healthScore);
    }

    @Test
    void shouldComputeHealthScore() {
        Item ok = new Item(UUID.randomUUID(), new Barcode("BC-1"), ItemStatus.OK, UUID.randomUUID(),
            null, null, null, null, "SN-1", LocalDate.now());
        Item damaged = new Item(UUID.randomUUID(), new Barcode("BC-2"), ItemStatus.DAMAGED, null,
            null, null, null, null, null, LocalDate.now());
        when(findAllItems.execute()).thenReturn(List.of(ok, damaged));

        InventoryHealthTool tool = new InventoryHealthTool(findAllItems);

        InventoryHealthTool.InventoryHealthReport report = tool.getInventoryHealth(null, null);

        assertEquals(2, report.totalItems);
        assertEquals(1, report.damagedItems);
        assertEquals(50.0, report.healthScore);
        assertEquals(1, report.itemsWithoutUnitAssignment);
        assertEquals(1, report.itemsWithoutSerialNumber);
    }
}
