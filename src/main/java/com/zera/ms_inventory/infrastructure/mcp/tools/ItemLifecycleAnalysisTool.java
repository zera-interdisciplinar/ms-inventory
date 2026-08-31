package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.usecase.item.FindItemById;

import io.modelcontextprotocol.server.mcp.annotation.McpTool;
import io.modelcontextprotocol.server.mcp.annotation.McpToolParam;

@Component
public class ItemLifecycleAnalysisTool {

    private final FindItemById findItemById;

    public ItemLifecycleAnalysisTool(FindItemById findItemById) {
        this.findItemById = findItemById;
    }

    @McpTool(
        name = "item_lifecycle_analysis",
        description = "Analyze the lifecycle of a specific item including age, usage intensity, and maintenance timeline",
        annotations = @McpTool.McpAnnotations(
            readOnlyHint = true,
            title = "Item Lifecycle Analysis"
        )
    )
    public ItemLifecycleReport analyzeItemLifecycle(
            @McpToolParam(description = "UUID of the item to analyze", required = true) UUID itemId) {

        Item item = findItemById.execute(itemId);
        LocalDate today = LocalDate.now();

        long ageInDays = 0;
        if (item.getAcquiredAt() != null) {
            ageInDays = ChronoUnit.DAYS.between(item.getAcquiredAt(), today);
        }

        return new ItemLifecycleReport(
            item.getId(),
            item.getSerialNumber(),
            item.getStatus().name(),
            item.getAcquiredAt(),
            ageInDays,
            item.getUsageIntensity() != null ? item.getUsageIntensity() : 0,
            item.getManufacturingDate(),
            item.getUnitId()
        );
    }

    public static class ItemLifecycleReport {
        public final UUID itemId;
        public final String serialNumber;
        public final String currentStatus;
        public final LocalDate acquiredAt;
        public final long ageInDays;
        public final Integer usageIntensity;
        public final Integer manufacturingDate;
        public final UUID unitId;

        public ItemLifecycleReport(UUID itemId, String serialNumber, String currentStatus, LocalDate acquiredAt,
                                  long ageInDays, Integer usageIntensity, Integer manufacturingDate, UUID unitId) {
            this.itemId = itemId;
            this.serialNumber = serialNumber;
            this.currentStatus = currentStatus;
            this.acquiredAt = acquiredAt;
            this.ageInDays = ageInDays;
            this.usageIntensity = usageIntensity;
            this.manufacturingDate = manufacturingDate;
            this.unitId = unitId;
        }
    }
}
