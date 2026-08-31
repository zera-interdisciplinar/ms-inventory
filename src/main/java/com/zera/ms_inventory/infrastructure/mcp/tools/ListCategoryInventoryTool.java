package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
            @McpToolParam(description = "Maximum number of results", required = false) Integer limit,
            @McpToolParam(description = "Pagination offset", required = false) Integer offset,
            @McpToolParam(description = "Filter from date (ISO format)", required = false) String fromDate,
            @McpToolParam(description = "Filter to date (ISO format)", required = false) String toDate) {

        List<Category> categories = findAllCategories.execute();
        List<Item> items = findAllItems.execute();

        int actualOffset = offset != null ? offset : 0;
        int actualLimit = limit != null ? limit : 100;

        return categories.stream()
            .skip(actualOffset)
            .limit(actualLimit)
            .map(category -> buildSummary(category, items))
            .collect(Collectors.toList());
    }

    private CategoryInventorySummary buildSummary(Category category, List<Item> allItems) {
        long totalItems = allItems.size();
        long damagedItems = allItems.stream()
            .filter(item -> item.getStatus().name().equals("DAMAGED"))
            .count();
        long okItems = totalItems - damagedItems;

        return new CategoryInventorySummary(
            category.getId(),
            category.getName(),
            category.getDescription(),
            totalItems,
            okItems,
            damagedItems
        );
    }

    public static class CategoryInventorySummary {
        public final java.util.UUID categoryId;
        public final String categoryName;
        public final String description;
        public final long totalItems;
        public final long okItems;
        public final long damagedItems;

        public CategoryInventorySummary(java.util.UUID categoryId, String categoryName, String description,
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
