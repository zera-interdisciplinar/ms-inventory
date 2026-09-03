package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.usecase.item.SemanticSearchInventory;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;

@Component
public class SemanticSearchInventoryTool {

    private static final int DEFAULT_LIMIT = 10;

    private final SemanticSearchInventory semanticSearchInventory;

    public SemanticSearchInventoryTool(SemanticSearchInventory semanticSearchInventory) {
        this.semanticSearchInventory = semanticSearchInventory;
    }

    @McpTool(
        name = "semantic_search_inventory",
        description = "Find inventory items by describing the equipment in natural language "
                + "(what it is, what it does, what it is made of, its manufacturer or category). "
                + "Matches meaning, not exact text. Use search_inventory instead for an exact status or serial number.",
        annotations = @McpTool.McpAnnotations(
            readOnlyHint = true,
            title = "Semantic Search Inventory"
        )
    )
    public List<Item> semanticSearch(
            @McpToolParam(description = McpToolScope.UNIT_ID_DESCRIPTION, required = true) UUID unitId,
            @McpToolParam(description = "Natural language description of the equipment being looked for", required = true) String query,
            @McpToolParam(description = "Maximum number of models to match (default: 10)", required = false) Integer limit) {

        int actualLimit = limit != null && limit > 0 ? limit : DEFAULT_LIMIT;
        return semanticSearchInventory.execute(McpToolScope.require(unitId), query, actualLimit);
    }
}
