package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.Category;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.usecase.category.FindAllCategories;
import com.zera.ms_inventory.core.usecase.item.FindAllItems;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;

@Component
public class ListCategoryInventoryTool {

    private final FindAllCategories findAllCategories;
    private final FindAllItems findAllItems;

    public ListCategoryInventoryTool(FindAllCategories findAllCategories, FindAllItems findAllItems) {
        this.findAllCategories = findAllCategories;
        this.findAllItems = findAllItems;
    }

    @McpTool(
        name = "list_category_inventory",
        description = "List inventory items grouped by category with item counts and status breakdown",
        annotations = @McpTool.McpAnnotations(
            readOnlyHint = true,
            title = "List Category Inventory"
        )
    )
    public List<CategoryInventorySummary> listCategoryInventory(
            @McpToolParam(description = McpToolScope.UNIT_ID_DESCRIPTION, required = true) UUID unitId,
            @McpToolParam(description = "Maximum number of results", required = false) Integer limit,
            @McpToolParam(description = "Pagination offset", required = false) Integer offset) {

        UUID scope = McpToolScope.require(unitId);
        List<Category> categories = findAllCategories.execute(scope);
        List<Item> items = findAllItems.execute(scope);

        int actualOffset = offset != null ? offset : 0;
        int actualLimit = limit != null ? limit : 100;

        return categories.stream()
            .skip(actualOffset)
            .limit(actualLimit)
            .map(category -> buildSummary(category, items))
            .collect(Collectors.toList());
    }
    
    private CategoryInventorySummary buildSummary(Category category, List<Item> allItems) {
        List<Item> categoryItems = allItems.stream()
            .filter(item -> belongsTo(item, category))
            .toList();

        long totalItems = categoryItems.size();
        long damagedItems = categoryItems.stream()
            .filter(item -> item.getStatus().name().equals("DAMAGED"))
            .count();

        return new CategoryInventorySummary(
            category.getId(),
            category.getName(),
            category.getDescription(),
            totalItems,
            totalItems - damagedItems,
            damagedItems
        );
    }

    private boolean belongsTo(Item item, Category category) {
        return item.getModel() != null
                && item.getModel().getCategory() != null
                && category.getId().equals(item.getModel().getCategory().getId());
    }

    public static class CategoryInventorySummary {
        public final UUID categoryId;
        public final String categoryName;
        public final String description;
        public final long totalItems;
        public final long okItems;
        public final long damagedItems;

        public CategoryInventorySummary(UUID categoryId, String categoryName, String description,
                                       long totalItems, long okItems, long damagedItems) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.description = description;
            this.totalItems = totalItems;
            this.okItems = okItems;
            this.damagedItems = damagedItems;
        }
    }
}
