package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.usecase.item.FindAllItems;

import io.modelcontextprotocol.server.mcp.annotation.McpTool;
import io.modelcontextprotocol.server.mcp.annotation.McpToolParam;

@Component
public class InventoryHealthTool {

    private final FindAllItems findAllItems;

    public InventoryHealthTool(FindAllItems findAllItems) {
        this.findAllItems = findAllItems;
    }

    @McpTool(
        name = "inventory_health",
        description = "Get overall inventory health score with statistics on item status, age, and condition",
        annotations = @McpTool.McpAnnotations(
            readOnlyHint = true,
            title = "Inventory Health"
        )
    )
    public InventoryHealthReport getInventoryHealth(
            @McpToolParam(description = "Filter from date (ISO format)", required = false) String fromDate,
            @McpToolParam(description = "Filter to date (ISO format)", required = false) String toDate) {

        List<Item> items = findAllItems.execute();

        if (items.isEmpty()) {
            return new InventoryHealthReport(0, 0, 0.0, 0, 0, 0, 0);
        }

        long totalItems = items.size();
        long damagedItems = items.stream()
            .filter(item -> item.getStatus().name().equals("DAMAGED"))
            .count();
        long okItems = totalItems - damagedItems;

        double healthScore = totalItems > 0 ? (okItems * 100.0) / totalItems : 0.0;

        long itemsWithoutUnitAssignment = items.stream()
            .filter(item -> item.getUnitId() == null)
            .count();

        long itemsWithoutSerialNumber = items.stream()
            .filter(item -> item.getSerialNumber() == null || item.getSerialNumber().isEmpty())
            .count();

        return new InventoryHealthReport(
            totalItems,
            damagedItems,
            healthScore,
            itemsWithoutUnitAssignment,
            itemsWithoutSerialNumber,
            okItems
        );
    }

    public static class InventoryHealthReport {
        public final long totalItems;
        public final long damagedItems;
        public final double healthScore;
        public final long itemsWithoutUnitAssignment;
        public final long itemsWithoutSerialNumber;
        public final long okItems;

        public InventoryHealthReport(long totalItems, long damagedItems, double healthScore,
                                     long itemsWithoutUnitAssignment, long itemsWithoutSerialNumber,
                                     long okItems) {
            this.totalItems = totalItems;
            this.damagedItems = damagedItems;
            this.healthScore = healthScore;
            this.itemsWithoutUnitAssignment = itemsWithoutUnitAssignment;
            this.itemsWithoutSerialNumber = itemsWithoutSerialNumber;
            this.okItems = okItems;
        }
    }
}
