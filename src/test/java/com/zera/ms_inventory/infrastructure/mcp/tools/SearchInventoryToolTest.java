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
class SearchInventoryToolTest {

    @Mock
    private FindAllItems findAllItems;

    private Item ok;
    private Item damaged;
    private Item noSerial;

    private void stub() {
        ok = new Item(UUID.randomUUID(), new Barcode("BC-1"), ItemStatus.OK, null,
            null, null, null, null, "ABC-123", LocalDate.now());
        damaged = new Item(UUID.randomUUID(), new Barcode("BC-2"), ItemStatus.DAMAGED, null,
            null, null, null, null, "XYZ-999", LocalDate.now());
        noSerial = new Item(UUID.randomUUID(), new Barcode("BC-3"), ItemStatus.OK, null,
            null, null, null, null, null, LocalDate.now());
        when(findAllItems.execute()).thenReturn(List.of(ok, damaged, noSerial));
    }

    @Test
    void shouldReturnAllWhenNoFilters() {
        stub();
        SearchInventoryTool tool = new SearchInventoryTool(findAllItems);

        assertEquals(3, tool.searchInventory(null, null).size());
    }

    @Test
    void shouldFilterByStatus() {
        stub();
        SearchInventoryTool tool = new SearchInventoryTool(findAllItems);

        List<Item> result = tool.searchInventory("damaged", null);

        assertEquals(1, result.size());
        assertEquals(damaged, result.get(0));
    }

    @Test
    void shouldReturnEmptyForInvalidStatus() {
        stub();
        SearchInventoryTool tool = new SearchInventoryTool(findAllItems);

        assertEquals(0, tool.searchInventory("BOGUS", null).size());
    }

    @Test
    void shouldFilterBySerialNumberIgnoringNulls() {
        stub();
        SearchInventoryTool tool = new SearchInventoryTool(findAllItems);

        List<Item> result = tool.searchInventory(null, "abc");

        assertEquals(1, result.size());
        assertEquals(ok, result.get(0));
    }
}
