package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.usecase.item.FindItemById;

import io.modelcontextprotocol.server.mcp.annotation.McpTool;
import io.modelcontextprotocol.server.mcp.annotation.McpToolParam;

@Component
public class GetItemDetailsTool {

    private final FindItemById findItemById;

    public GetItemDetailsTool(FindItemById findItemById) {
        this.findItemById = findItemById;
    }

    @McpTool(
        name = "get_item_details",
        description = "Retrieve detailed information about a specific inventory item including status, dates, and assigned unit",
        annotations = @McpTool.McpAnnotations(
            readOnlyHint = true,
            title = "Get Item Details"
        )
    )
    public Item getItemDetails(
            @McpToolParam(description = "UUID of the item to retrieve", required = true) UUID itemId) {
        return findItemById.execute(itemId);
    }
}
