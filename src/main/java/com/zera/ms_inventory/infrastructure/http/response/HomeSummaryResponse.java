package com.zera.ms_inventory.infrastructure.http.response;

import com.zera.ms_inventory.core.domain.valueobject.HomeSummary;

/**
 * {@code occupancyPercent} nulo significa unidade sem capacidade configurada;
 * {@code activeItemsChangePercent} nulo significa que nao havia estoque na janela para comparar.
 * {@code recentItems} vem paginado, com o total, para o app poder virar a lista sem endpoint novo.
 */
public record HomeSummaryResponse(
        long activeItems,
        Double activeItemsChangePercent,
        Integer stockCapacity,
        Double occupancyPercent,
        long pendingApproval,
        long inMaintenance,
        long awaitingEvaluation,
        long disposalsInWindow,
        int windowDays,
        PageResponse<ItemResponse> recentItems
) {
    public static HomeSummaryResponse from(HomeSummary summary, ItemResponses itemResponses) {
        if (summary == null) {
            return null;
        }
        return new HomeSummaryResponse(summary.activeItems(), round(summary.activeItemsChangePercent()),
                summary.stockCapacity(), round(summary.occupancyPercent()), summary.pendingApproval(),
                summary.inMaintenance(), summary.awaitingEvaluation(), summary.disposalsInWindow(),
                summary.windowDays(), PageResponse.from(summary.recentItems(), itemResponses::from));
    }

    private static Double round(Double value) {
        return value == null ? null
                : java.math.BigDecimal.valueOf(value).setScale(2, java.math.RoundingMode.HALF_UP).doubleValue();
    }
}
