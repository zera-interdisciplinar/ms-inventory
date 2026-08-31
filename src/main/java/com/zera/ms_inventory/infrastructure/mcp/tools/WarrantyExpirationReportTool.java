package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.usecase.item.FindAllItems;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;

@Component
public class WarrantyExpirationReportTool {

    private final FindAllItems findAllItems;

    public WarrantyExpirationReportTool(FindAllItems findAllItems) {
        this.findAllItems = findAllItems;
    }

    @McpTool(
        name = "warranty_expiration_report",
        description = "Report on items with warranties expiring within a specified number of days",
        annotations = @McpTool.McpAnnotations(
            readOnlyHint = true,
            title = "Warranty Expiration Report"
        )
    )
    public List<WarrantyExpiringItem> getWarrantyExpirationReport(
            @McpToolParam(description = "Days ahead to check for expiration (default: 30)", required = false) Integer daysAhead,
            @McpToolParam(description = "Maximum number of results", required = false) Integer limit,
            @McpToolParam(description = "Pagination offset", required = false) Integer offset) {

        int checkDays = daysAhead != null ? daysAhead : 30;
        int actualLimit = limit != null ? limit : 100;
        int actualOffset = offset != null ? offset : 0;

        LocalDate today = LocalDate.now();
        LocalDate expirationDeadline = today.plusDays(checkDays);

        List<Item> items = findAllItems.execute();

        return items.stream()
            .filter(item -> {
                if (item.getAcquiredAt() == null) return false;
                // Warranty expiration would be acquiredAt + warrantyMonths
                // For now, filter by items acquired within last N years
                LocalDate potentialExpiry = item.getAcquiredAt().plusYears(3);
                return !potentialExpiry.isBefore(today) && !potentialExpiry.isAfter(expirationDeadline);
            })
            .skip(actualOffset)
            .limit(actualLimit)
            .map(item -> new WarrantyExpiringItem(
                item.getId(),
                item.getSerialNumber(),
                item.getAcquiredAt(),
                item.getAcquiredAt().plusYears(3) // Estimated expiry
            ))
            .collect(Collectors.toList());
    }

    public static class WarrantyExpiringItem {
        public final java.util.UUID itemId;
        public final String serialNumber;
        public final LocalDate acquiredAt;
        public final LocalDate estimatedExpiryDate;

        public WarrantyExpiringItem(java.util.UUID itemId, String serialNumber, LocalDate acquiredAt, LocalDate estimatedExpiryDate) {
            this.itemId = itemId;
            this.serialNumber = serialNumber;
            this.acquiredAt = acquiredAt;
            this.estimatedExpiryDate = estimatedExpiryDate;
        }
    }
}
