package com.zera.ms_inventory.infrastructure.http.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Disposal;
import com.zera.ms_inventory.core.domain.valueobject.DestinationType;
import com.zera.ms_inventory.core.domain.valueobject.DisposedItem;

public record DisposalResponse(
        UUID id,
        UUID unitId,
        DestinationType destination,
        String placeId,
        String placeName,
        LocalDate disposedAt,
        String notes,
        List<DisposedItemResponse> items,
        Double totalWeightKg,
        UUID createdBy,
        String createdByName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /** Peso de cada item congelado no descarte; nulo quando o modelo nao tem peso estimado. */
    public record DisposedItemResponse(UUID itemId, String displayCode, String name, Double weightKg) {
        static DisposedItemResponse from(DisposedItem item) {
            return new DisposedItemResponse(item.itemId(), item.displayCode(), item.name(), item.weightKg());
        }
    }

    public static DisposalResponse from(Disposal disposal) {
        if (disposal == null) {
            return null;
        }
        return new DisposalResponse(disposal.getId(), disposal.getUnitId(), disposal.getDestination(),
                disposal.getPlaceId(), disposal.getPlaceName(), disposal.getDisposedAt(), disposal.getNotes(),
                disposal.getItems().stream().map(DisposedItemResponse::from).toList(),
                disposal.totalWeightKg(), disposal.getCreatedBy(), disposal.getCreatedByName(),
                disposal.getCreatedAt(), disposal.getUpdatedAt());
    }
}
