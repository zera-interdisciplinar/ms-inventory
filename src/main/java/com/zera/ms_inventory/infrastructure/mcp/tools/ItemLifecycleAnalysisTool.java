package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.usecase.item.FindItemById;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;

@Component
public class ItemLifecycleAnalysisTool {

    private final FindItemById findItemById;

    public ItemLifecycleAnalysisTool(FindItemById findItemById) {
        this.findItemById = findItemById;
    }

    // a descricao anunciava usageIntensity como LOW/MEDIUM/HIGH, que e o formato antigo; o valor
    // sempre foi um inteiro, e o assistente traduzia errado ao explicar
    @McpTool(
        name = "item_lifecycle_analysis",
        description = "Analyze one item's lifecycle: age, usage intensity (integer scale 0 to 10), "
                + "physical condition, warranty expiry, expected end of life and predicted failure date, "
                + "with the days remaining for each",
        annotations = @McpTool.McpAnnotations(
            readOnlyHint = true,
            title = "Item Lifecycle Analysis"
        )
    )
    public ItemLifecycleReport analyzeItemLifecycle(
            @McpToolParam(description = McpToolScope.UNIT_ID_DESCRIPTION, required = true) UUID unitId,
            @McpToolParam(description = "UUID of the item to analyze", required = true) UUID itemId) {

        Item item = findItemById.execute(McpToolScope.require(unitId), itemId);
        LocalDate today = LocalDate.now();
        Model model = item.getModel();

        LocalDate warrantyExpiry = plusMonths(item.getAcquiredAt(),
                model == null ? null : model.getWarrantyMonths());
        LocalDate expectedEndOfLife = plusMonths(item.getAcquiredAt(),
                model == null ? null : model.getExpectedLifespanMonths());

        return new ItemLifecycleReport(
                McpItemView.of(item),
                daysBetween(item.getAcquiredAt(), today),
                warrantyExpiry,
                daysBetween(today, warrantyExpiry),
                expectedEndOfLife,
                daysBetween(today, expectedEndOfLife),
                daysBetween(today, item.getPredictedFailureDate()));
    }

    private static LocalDate plusMonths(LocalDate base, Integer months) {
        return base == null || months == null ? null : base.plusMonths(months);
    }

    /** Negativo quando a data ja passou, que e como o assistente descobre garantia vencida. */
    private static Long daysBetween(LocalDate from, LocalDate to) {
        return from == null || to == null ? null : ChronoUnit.DAYS.between(from, to);
    }

    /**
     * Os dias restantes vem calculados, e nao so as datas: pedir ao modelo para subtrair datas e
     * onde ele erra, e o erro chega ao gestor como "ainda tem garantia".
     */
    public record ItemLifecycleReport(
            McpItemView item,
            Long ageInDays,
            LocalDate warrantyExpiresOn,
            Long daysUntilWarrantyExpires,
            LocalDate expectedEndOfLifeOn,
            Long daysUntilExpectedEndOfLife,
            Long daysUntilPredictedFailure
    ) {
    }
}
