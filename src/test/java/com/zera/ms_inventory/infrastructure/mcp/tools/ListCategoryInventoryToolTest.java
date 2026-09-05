package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Category;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.usecase.category.FindAllCategories;
import com.zera.ms_inventory.core.usecase.item.FindAllItems;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListCategoryInventoryToolTest {

    @Mock
    private FindAllCategories findAllCategories;

    @Mock
    private FindAllItems findAllItems;

    private Model modelOf(Category category) {
        return new Model(UUID.randomUUID(), Fixtures.UNIT, "Laptop", "Acme", 24, 60, Set.of(), category);
    }

    private Item itemOf(Model model, ItemStatus status) {
        return new Item(UUID.randomUUID(), new Barcode("123456"), status, Fixtures.UNIT, model,
                LocalDateTime.now(), 2024, 7, "SN-001", LocalDate.now());
    }

    @Test
    void shouldCountOnlyTheItemsThatReachEachCategory() {
        Category electronics = Fixtures.category(UUID.randomUUID(), Fixtures.UNIT);
        Category furniture = new Category(UUID.randomUUID(), Fixtures.UNIT, "Furniture", "Chairs and desks");
        Model laptop = modelOf(electronics);

        when(findAllCategories.execute(Fixtures.UNIT)).thenReturn(List.of(electronics, furniture));
        when(findAllItems.execute(Fixtures.UNIT)).thenReturn(List.of(
                itemOf(laptop, ItemStatus.OK),
                itemOf(laptop, ItemStatus.DAMAGED)));

        List<ListCategoryInventoryTool.CategoryInventorySummary> result =
                new ListCategoryInventoryTool(findAllCategories, findAllItems)
                        .listCategoryInventory(Fixtures.UNIT, null, null);

        assertEquals(2, result.size());
        assertEquals(2, result.get(0).totalItems);
        assertEquals(1, result.get(0).okItems);
        assertEquals(1, result.get(0).damagedItems);
        // a categoria sem modelo nenhum nao herda a contagem global
        assertEquals(0, result.get(1).totalItems);
    }

    @Test
    void shouldIgnoreItemsWithoutModel() {
        Category electronics = Fixtures.category(UUID.randomUUID(), Fixtures.UNIT);
        when(findAllCategories.execute(Fixtures.UNIT)).thenReturn(List.of(electronics));
        when(findAllItems.execute(Fixtures.UNIT)).thenReturn(List.of(itemOf(null, ItemStatus.OK)));

        List<ListCategoryInventoryTool.CategoryInventorySummary> result =
                new ListCategoryInventoryTool(findAllCategories, findAllItems)
                        .listCategoryInventory(Fixtures.UNIT, 10, 0);

        assertEquals(0, result.get(0).totalItems);
    }

    @Test
    void shouldRejectMissingUnitIdInsteadOfFallingBackToAGlobalRead() {
        ListCategoryInventoryTool tool = new ListCategoryInventoryTool(findAllCategories, findAllItems);

        assertThrows(IllegalArgumentException.class, () -> tool.listCategoryInventory(null, null, null));
        verifyNoInteractions(findAllCategories);
        verifyNoInteractions(findAllItems);
    }
}
