package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.ItemCondition;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.usecase.item.FindAllItems;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;

@Component
public class InventoryHealthTool {

    /** Previsao dentro desta janela conta como quebra proxima no resumo. */
    private static final int FAILURE_HORIZON_DAYS = 90;

    private final FindAllItems findAllItems;

    public InventoryHealthTool(FindAllItems findAllItems) {
        this.findAllItems = findAllItems;
    }

    // o relatorio antigo so conhecia "OK ou DAMAGED" e contava itens sem unidade, numero que e
    // sempre zero porque toda leitura ja e escopada por unidade
    @McpTool(
        name = "inventory_health",
        description = "Overall inventory health for the unit: item counts per status and per physical "
                + "condition, how many items are waiting for approval or in maintenance, how many lack a "
                + "failure prediction and how many are predicted to fail within 90 days",
        annotations = @McpTool.McpAnnotations(
            readOnlyHint = true,
            title = "Inventory Health"
        )
    )
    public InventoryHealthReport getInventoryHealth(
            @McpToolParam(description = McpToolScope.UNIT_ID_DESCRIPTION, required = true) UUID unitId) {

        List<Item> items = findAllItems.execute(McpToolScope.require(unitId));
        LocalDate horizon = LocalDate.now().plusDays(FAILURE_HORIZON_DAYS);

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (ItemStatus status : ItemStatus.values()) {
            long count = items.stream().filter(item -> item.getStatus() == status).count();
            if (count > 0) {
                byStatus.put(status.name(), count);
            }
        }

        Map<String, Long> byCondition = new LinkedHashMap<>();
        for (ItemCondition condition : ItemCondition.values()) {
            long count = items.stream().filter(item -> item.getCondition() == condition).count();
            if (count > 0) {
                byCondition.put(condition.name(), count);
            }
        }

        long usable = items.stream()
                .filter(item -> item.getStatus() != null && item.getStatus().isActive())
                .filter(item -> item.getCondition() != ItemCondition.DAMAGED)
                .count();

        return new InventoryHealthReport(
                items.size(),
                byStatus,
                byCondition,
                // saude e o que ainda serve sobre o que existe, e nao "nao danificado": um item em
                // manutencao esta intacto e mesmo assim nao esta disponivel
                items.isEmpty() ? 0.0 : (usable * 100.0) / items.size(),
                count(items, item -> item.getStatus() == ItemStatus.PENDING_APPROVAL),
                count(items, item -> item.getStatus() == ItemStatus.IN_MAINTENANCE),
                count(items, item -> item.getSerialNumber() == null || item.getSerialNumber().isBlank()),
                count(items, item -> item.getPredictedFailureDate() == null),
                count(items, item -> item.getPredictedFailureDate() != null
                        && !item.getPredictedFailureDate().isAfter(horizon)));
    }

    private static long count(List<Item> items, java.util.function.Predicate<Item> predicate) {
        return items.stream().filter(predicate).count();
    }

    public record InventoryHealthReport(
            long totalItems,
            Map<String, Long> itemsByStatus,
            Map<String, Long> itemsByCondition,
            double healthScore,
            long itemsAwaitingApproval,
            long itemsInMaintenance,
            long itemsWithoutSerialNumber,
            long itemsWithoutPrediction,
            long itemsPredictedToFailWithin90Days
    ) {
    }
}
