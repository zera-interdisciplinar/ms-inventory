package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
            @McpToolParam(description = McpToolScope.UNIT_ID_DESCRIPTION, required = true) UUID unitId,
            @McpToolParam(description = "Days ahead to check for expiration (default: 30)", required = false) Integer daysAhead,
            @McpToolParam(description = "Maximum number of results", required = false) Integer limit,
            @McpToolParam(description = "Pagination offset", required = false) Integer offset) {

        int checkDays = daysAhead != null ? daysAhead : 30;
        int actualLimit = limit != null ? limit : 100;
        int actualOffset = offset != null ? offset : 0;

        LocalDate today = LocalDate.now();
        LocalDate deadline = today.plusDays(checkDays);

        List<WarrantyExpiringItem> expiring = new ArrayList<>();
        for (Item item : findAllItems.execute(McpToolScope.require(unitId))) {
            LocalDate expiry = warrantyExpiry(item);
            if (expiry == null || expiry.isBefore(today) || expiry.isAfter(deadline)) {
                continue;
            }
            expiring.add(new WarrantyExpiringItem(item.getId(), item.getSerialNumber(), item.getAcquiredAt(), expiry));
        }

        return expiring.stream().skip(actualOffset).limit(actualLimit).toList();
    }

    private LocalDate warrantyExpiry(Item item) {
        if (item.getAcquiredAt() == null || item.getModel() == null
                || item.getModel().getWarrantyMonths() == null) {
            return null;
        }
        return item.getAcquiredAt().plusMonths(item.getModel().getWarrantyMonths());
    }

    public static class WarrantyExpiringItem {
        public final UUID itemId;
        public final String serialNumber;
        public final LocalDate acquiredAt;
        public final LocalDate expiryDate;

        public WarrantyExpiringItem(UUID itemId, String serialNumber, LocalDate acquiredAt, LocalDate expiryDate) {
            this.itemId = itemId;
            this.serialNumber = serialNumber;
            this.acquiredAt = acquiredAt;
            this.expiryDate = expiryDate;
        }
    }
}
