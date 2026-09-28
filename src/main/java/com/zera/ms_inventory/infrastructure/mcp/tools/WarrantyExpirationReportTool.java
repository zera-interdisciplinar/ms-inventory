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
        description = "Report on items whose warranty expires within a number of days, with the short "
                + "code, model and days remaining for each. Warranty comes from the model, counted from "
                + "the item's acquisition date; items without either are left out.",
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
        int actualOffset = Math.max(0, offset != null ? offset : 0);

        LocalDate today = LocalDate.now();
        LocalDate deadline = today.plusDays(checkDays);

        List<WarrantyExpiringItem> expiring = new ArrayList<>();
        for (Item item : findAllItems.execute(McpToolScope.require(unitId))) {
            LocalDate expiry = warrantyExpiry(item);
            if (expiry == null || expiry.isBefore(today) || expiry.isAfter(deadline)) {
                continue;
            }
            expiring.add(new WarrantyExpiringItem(item.getId(), item.getDisplayCode(), item.getName(),
                    item.getModel().getName(), item.getSerialNumber(), item.getStatus().name(),
                    item.getAcquiredAt(), expiry,
                    java.time.temporal.ChronoUnit.DAYS.between(today, expiry)));
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

    /**
     * O relatorio so identificava o item pelo numero de serie, que e opcional no v1: item sem
     * serie aparecia como uma linha sem nome. O codigo curto e o que o gestor le na etiqueta.
     */
    public record WarrantyExpiringItem(
            UUID itemId,
            String displayCode,
            String name,
            String modelName,
            String serialNumber,
            String status,
            LocalDate acquiredAt,
            LocalDate expiryDate,
            long daysUntilExpiry
    ) {
    }
}
