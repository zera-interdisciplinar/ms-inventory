package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.core.domain.valueobject.ItemCondition;
import com.zera.ms_inventory.core.domain.valueobject.ItemFilter;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
import com.zera.ms_inventory.core.usecase.item.ListItems;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchInventoryToolTest {

    @Mock
    private ListItems listItems;

    private Item item(ItemStatus status, String serialNumber) {
        return new Item(UUID.randomUUID(), new Barcode("123456"), status, Fixtures.UNIT,
                Fixtures.model(Fixtures.UNIT), null, 2024, 6, serialNumber, LocalDate.now());
    }

    private void returning(Item... items) {
        when(listItems.execute(any(), any(), any()))
                .thenReturn(new PageResult<>(List.of(items), 0, 20, items.length));
    }

    @Test
    void shouldReturnTheItemsOfThePageWithTheTotal() {
        returning(item(ItemStatus.IN_STOCK, "SN-001"), item(ItemStatus.IN_MAINTENANCE, "SN-002"));

        var page = new SearchInventoryTool(listItems)
                .searchInventory(Fixtures.UNIT, null, null, null, null, null);

        assertEquals(2, page.items().size());
        assertEquals(2, page.totalItems());
        assertEquals("IN_STOCK", page.items().get(0).status());
    }

    /** O filtro desce para o banco: a ferramenta nao varre mais a unidade inteira em memoria. */
    @Test
    void shouldPushStatusConditionAndQueryDownToTheRepository() {
        returning();

        new SearchInventoryTool(listItems)
                .searchInventory(Fixtures.UNIT, "in_maintenance", "damaged", "notebook", null, null);

        ArgumentCaptor<ItemFilter> filter = ArgumentCaptor.forClass(ItemFilter.class);
        verify(listItems).execute(any(), filter.capture(), any());
        assertEquals(ItemStatus.IN_MAINTENANCE, filter.getValue().status());
        assertEquals(ItemCondition.DAMAGED, filter.getValue().condition());
        assertEquals("notebook", filter.getValue().query());
    }

    /** O assistente erra o nome do enum de vez em quando; isso vira "sem filtro", nao erro 500. */
    @Test
    void shouldIgnoreAnUnknownStatusOrConditionInsteadOfFailing() {
        returning();

        new SearchInventoryTool(listItems)
                .searchInventory(Fixtures.UNIT, "NOPE", "ALSO_NOPE", null, null, null);

        ArgumentCaptor<ItemFilter> filter = ArgumentCaptor.forClass(ItemFilter.class);
        verify(listItems).execute(any(), filter.capture(), any());
        assertNull(filter.getValue().status());
        assertNull(filter.getValue().condition());
    }

    @Test
    void shouldCapThePageSizeSoOneCallCannotDrainTheUnit() {
        returning();

        new SearchInventoryTool(listItems).searchInventory(Fixtures.UNIT, null, null, null, 5000, -3);

        ArgumentCaptor<Pagination> pagination = ArgumentCaptor.forClass(Pagination.class);
        verify(listItems).execute(any(), any(), pagination.capture());
        assertEquals(Pagination.MAX_SIZE, pagination.getValue().size());
        assertEquals(0, pagination.getValue().page());
    }

    @Test
    void shouldRejectMissingUnitIdInsteadOfFallingBackToAGlobalRead() {
        SearchInventoryTool tool = new SearchInventoryTool(listItems);

        assertThrows(IllegalArgumentException.class,
                () -> tool.searchInventory(null, null, null, null, null, null));
        verifyNoInteractions(listItems);
    }
}
