package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.usecase.item.FindItemById;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;

@Component
public class GetItemDetailsTool {

    private final FindItemById findItemById;

    public GetItemDetailsTool(FindItemById findItemById) {
        this.findItemById = findItemById;
    }

    @McpTool(
        name = "get_item_details",
        description = "Retrieve one inventory item: short code, name, status, physical condition, "
                + "damages, model, category, usage intensity (0-10) and predicted failure date",
        annotations = @McpTool.McpAnnotations(
            readOnlyHint = true,
            title = "Get Item Details"
        )
    )
    public McpItemView getItemDetails(
            @McpToolParam(description = McpToolScope.UNIT_ID_DESCRIPTION, required = true) UUID unitId,
            @McpToolParam(description = "UUID of the item to retrieve", required = true) UUID itemId) {
        return McpItemView.of(findItemById.execute(McpToolScope.require(unitId), itemId));
    }
}
