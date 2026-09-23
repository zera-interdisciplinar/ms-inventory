package com.zera.ms_inventory.infrastructure.http.response;

import java.util.List;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.WorkCenterSummary;

/** Cada pendencia leva o que a tela precisa para agir: o que falta no rascunho e por que reprovou. */
public record WorkCenterResponse(
        List<PendingItemResponse> drafts,
        List<PendingItemResponse> rejected,
        List<PendingItemResponse> damagedWithoutDestination,
        long inMaintenance,
        long awaitingEvaluation
) {
    public record PendingItemResponse(
            UUID id,
            String displayCode,
            String name,
            String modelName,
            List<String> missingFields,
            String rejectionReason,
            java.time.LocalDateTime updatedAt
    ) {
        static PendingItemResponse from(Item item, String rejectionReason) {
            return new PendingItemResponse(item.getId(), item.getDisplayCode(), item.getName(),
                    item.getModel() != null ? item.getModel().getName() : null,
                    item.missingRequiredFields(), rejectionReason, item.getUpdatedAt());
        }
    }

    public static WorkCenterResponse from(WorkCenterSummary summary) {
        if (summary == null) {
            return null;
        }
        return new WorkCenterResponse(
                summary.drafts().stream().map(item -> PendingItemResponse.from(item, null)).toList(),
                summary.rejected().stream()
                        .map(item -> PendingItemResponse.from(item, summary.rejectionReasons().get(item.getId())))
                        .toList(),
                summary.damagedWithoutDestination().stream()
                        .map(item -> PendingItemResponse.from(item, null)).toList(),
                summary.inMaintenance(), summary.awaitingEvaluation());
    }
}
