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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchInventoryToolTest {

    @Mock
    private FindAllItems findAllItems;

    private Item item(ItemStatus status, String serialNumber) {
        return new Item(UUID.randomUUID(), new Barcode("123456"), status, Fixtures.UNIT,
                Fixtures.model(Fixtures.UNIT), LocalDateTime.now(), 2024, 7, serialNumber, LocalDate.now());
    }

    @Test
    void shouldReturnEverythingInTheUnitWhenNoFilterIsGiven() {
        when(findAllItems.execute(Fixtures.UNIT))
                .thenReturn(List.of(item(ItemStatus.OK, "SN-001"), item(ItemStatus.DAMAGED, "SN-002")));

        List<Item> result = new SearchInventoryTool(findAllItems).searchInventory(Fixtures.UNIT, null, null);

        assertEquals(2, result.size());
    }

    @Test
    void shouldFilterByStatus() {
        when(findAllItems.execute(Fixtures.UNIT))
                .thenReturn(List.of(item(ItemStatus.OK, "SN-001"), item(ItemStatus.DAMAGED, "SN-002")));

        List<Item> result = new SearchInventoryTool(findAllItems).searchInventory(Fixtures.UNIT, "damaged", null);

        assertEquals(1, result.size());
        assertEquals(ItemStatus.DAMAGED, result.get(0).getStatus());
    }

    @Test
    void shouldReturnNothingForAnUnknownStatus() {
        when(findAllItems.execute(Fixtures.UNIT)).thenReturn(List.of(item(ItemStatus.OK, "SN-001")));

        assertTrue(new SearchInventoryTool(findAllItems).searchInventory(Fixtures.UNIT, "NOPE", null).isEmpty());
    }

    @Test
    void shouldFilterBySerialNumberSubstringIgnoringCaseAndNulls() {
        when(findAllItems.execute(Fixtures.UNIT))
                .thenReturn(List.of(item(ItemStatus.OK, "SN-ABC"), item(ItemStatus.OK, null)));

        List<Item> result = new SearchInventoryTool(findAllItems).searchInventory(Fixtures.UNIT, null, "abc");

        assertEquals(1, result.size());
        assertEquals("SN-ABC", result.get(0).getSerialNumber());
    }

    @Test
    void shouldRejectMissingUnitIdInsteadOfFallingBackToAGlobalRead() {
        SearchInventoryTool tool = new SearchInventoryTool(findAllItems);

        assertThrows(IllegalArgumentException.class, () -> tool.searchInventory(null, null, null));
        verifyNoInteractions(findAllItems);
    }
}
