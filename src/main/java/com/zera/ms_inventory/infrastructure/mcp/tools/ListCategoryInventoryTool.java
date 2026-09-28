package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.Category;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.ItemCondition;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
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

    // o resumo dividia a categoria em "ok" e "danificado", que era o modelo antigo de duas
    // condicoes; agora sao quatro, e um item descartado nao deve contar como estoque
    @McpTool(
        name = "list_category_inventory",
        description = "List the unit's categories with, for each one, how many items exist, how many are "
                + "still in stock, how many are damaged and how many were already disposed of",
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

        int actualOffset = Math.max(0, offset != null ? offset : 0);
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

        return new CategoryInventorySummary(
            category.getId(),
            category.getName(),
            category.getDescription(),
            categoryItems.size(),
            count(categoryItems, item -> item.getStatus() == ItemStatus.IN_STOCK),
            count(categoryItems, item -> item.getCondition() == ItemCondition.DAMAGED
                    || item.getCondition() == ItemCondition.SEMI_DAMAGED),
            count(categoryItems, item -> item.getStatus() == ItemStatus.DISPOSED)
        );
    }

    private static long count(List<Item> items, java.util.function.Predicate<Item> predicate) {
        return items.stream().filter(predicate).count();
    }

    private boolean belongsTo(Item item, Category category) {
        return item.getModel() != null
                && item.getModel().getCategory() != null
                && category.getId().equals(item.getModel().getCategory().getId());
    }

    public record CategoryInventorySummary(
            UUID categoryId,
            String categoryName,
            String description,
            long totalItems,
            long itemsInStock,
            long damagedItems,
            long disposedItems
    ) {
    }
}
