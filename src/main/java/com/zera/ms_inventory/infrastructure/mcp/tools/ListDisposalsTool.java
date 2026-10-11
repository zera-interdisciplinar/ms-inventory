package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.Disposal;
import com.zera.ms_inventory.core.domain.valueobject.DisposalFilter;
import com.zera.ms_inventory.core.domain.valueobject.DisposedItem;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
import com.zera.ms_inventory.core.usecase.disposal.ListDisposals;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * Descarte nao existia quando estas ferramentas foram escritas, entao o assistente nao tinha como
 * responder para onde um item foi nem quanto peso saiu da unidade.
 */
@Component
public class ListDisposalsTool {

    private static final int DEFAULT_LIMIT = 20;

    private final ListDisposals listDisposals;

    public ListDisposalsTool(ListDisposals listDisposals) {
        this.listDisposals = listDisposals;
    }

    @McpTool(
        name = "list_disposals",
        description = "List the unit's disposals, most recent first: destination (RECYCLING, LANDFILL, "
                + "DONATION, RESALE or RETURN_TO_MANUFACTURER), place, date, the items included and the "
                + "total weight in kilograms",
        annotations = @McpTool.McpAnnotations(
            readOnlyHint = true,
            title = "List Disposals"
        )
    )
    public DisposalPage listDisposals(
            @McpToolParam(description = McpToolScope.UNIT_ID_DESCRIPTION, required = true) UUID unitId,
            @McpToolParam(description = "Maximum number of results (default: 20, max: 100)", required = false)
                    Integer limit,
            @McpToolParam(description = "Page number, zero-based (default: 0)", required = false) Integer page) {

        int size = Math.clamp(limit == null ? DEFAULT_LIMIT : limit, 1, Pagination.MAX_SIZE);
        Pagination pagination = new Pagination(Math.max(0, page == null ? 0 : page), size);

        PageResult<DisposalSummary> result =
                listDisposals.execute(McpToolScope.require(unitId), DisposalFilter.none(), pagination)
                        .map(ListDisposalsTool::summaryOf);
        return new DisposalPage(result.content(), result.totalElements(), result.page(), result.size());
    }

    private static DisposalSummary summaryOf(Disposal disposal) {
        List<DisposedItem> items = disposal.getItems();
        // item cujo modelo nao tem peso estimado entra na contagem mas nao no total de kg; somar
        // zero mentiria menos, mas ainda mentiria
        double totalWeightKg = items.stream()
                .map(DisposedItem::weightKg)
                .filter(java.util.Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();

        return new DisposalSummary(
                disposal.getId(),
                disposal.getDestination() == null ? null : disposal.getDestination().name(),
                disposal.getPlaceName(),
                disposal.getDisposedAt(),
                items.size(),
                totalWeightKg,
                items.stream().filter(item -> item.weightKg() == null).count(),
                items.stream()
                        .map(item -> new DisposedItemView(item.itemId(), item.displayCode(), item.name(),
                                item.weightKg()))
                        .toList());
    }

    public record DisposalPage(List<DisposalSummary> disposals, long totalDisposals, int page, int size) {
    }

    public record DisposalSummary(
            UUID disposalId,
            String destination,
            String placeName,
            LocalDate disposedAt,
            int itemCount,
            double totalWeightKg,
            long itemsWithoutWeight,
            List<DisposedItemView> items
    ) {
    }

    public record DisposedItemView(UUID itemId, String displayCode, String name, Double weightKg) {
    }
}
