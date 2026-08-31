package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.usecase.item.FindAllItems;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;

@Component
public class SearchInventoryTool {

    private final FindAllItems findAllItems;

    public SearchInventoryTool(FindAllItems findAllItems) {
        this.findAllItems = findAllItems;
    }

    @McpTool(
        name = "search_inventory",
        description = "Search inventory items by status and/or serial number. Returns matching items with their details.",
        annotations = @McpTool.McpAnnotations(
            readOnlyHint = true,
            title = "Search Inventory"
        )
    )
    public List<Item> searchInventory(
            @McpToolParam(description = "Filter by item status (DAMAGED or OK). Optional.", required = false) String status,
            @McpToolParam(description = "Filter by serial number (partial match, case-insensitive). Optional.", required = false) String serialNumber) {

        List<Item> allItems = findAllItems.execute();

        return allItems.stream()
            .filter(item -> status == null || matchesStatus(item, status))
            .filter(item -> serialNumber == null || matchesSerialNumber(item, serialNumber))
            .collect(Collectors.toList());
    }

    private boolean matchesStatus(Item item, String status) {
        try {
            ItemStatus targetStatus = ItemStatus.valueOf(status.toUpperCase());
            return item.getStatus() == targetStatus;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean matchesSerialNumber(Item item, String serialNumber) {
        if (item.getSerialNumber() == null) {
            return false;
        }
        return item.getSerialNumber()
            .toLowerCase()
            .contains(serialNumber.toLowerCase());
    }
}
