package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.core.domain.entity.Category;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.usecase.category.FindAllCategories;
import com.zera.ms_inventory.core.usecase.item.FindAllItems;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListCategoryInventoryToolTest {

    @Mock
    private FindAllCategories findAllCategories;

    @Mock
    private FindAllItems findAllItems;

    @Test
    void shouldBuildSummaryPerCategory() {
        Category category = new Category(UUID.randomUUID(), "Tools", "Hand tools");
        Item ok = new Item(UUID.randomUUID(), new Barcode("BC-1"), ItemStatus.OK, UUID.randomUUID(),
            null, null, null, null, "SN-1", LocalDate.now());
        Item damaged = new Item(UUID.randomUUID(), new Barcode("BC-2"), ItemStatus.DAMAGED, null,
            null, null, null, null, "SN-2", LocalDate.now());
        when(findAllCategories.execute()).thenReturn(List.of(category));
        when(findAllItems.execute()).thenReturn(List.of(ok, damaged));

        ListCategoryInventoryTool tool = new ListCategoryInventoryTool(findAllCategories, findAllItems);

        List<ListCategoryInventoryTool.CategoryInventorySummary> result = tool.listCategoryInventory(null, null, null, null);

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).totalItems);
        assertEquals(1, result.get(0).damagedItems);
        assertEquals(1, result.get(0).okItems);
    }

    @Test
    void shouldApplyLimitAndOffset() {
        Category c1 = new Category(UUID.randomUUID(), "A", "desc");
        Category c2 = new Category(UUID.randomUUID(), "B", "desc");
        when(findAllCategories.execute()).thenReturn(List.of(c1, c2));
        when(findAllItems.execute()).thenReturn(List.of());

        ListCategoryInventoryTool tool = new ListCategoryInventoryTool(findAllCategories, findAllItems);

        List<ListCategoryInventoryTool.CategoryInventorySummary> result = tool.listCategoryInventory(1, 1, null, null);

        assertEquals(1, result.size());
        assertEquals(c2.getId(), result.get(0).categoryId);
    }
}
