package com.zera.ms_inventory.infrastructure.http.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;

public record ItemResponse(
        UUID id,
        String barcode,
        ItemStatus status,
        UUID unitId,
        ModelResponse model,
        String serialNumber,
        LocalDate acquiredAt,
        Integer manufacturingDate,
        Integer usageIntensity,
        LocalDateTime nextPredictionDate,
        LocalDateTime lastEventAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ItemResponse from(Item item) {
        if (item == null) {
            return null;
        }
        return new ItemResponse(item.getId(), item.getBarcode().getValue(), item.getStatus(), item.getUnitId(),
                ModelResponse.from(item.getModel()), item.getSerialNumber(), item.getAcquiredAt(),
                item.getManufacturingDate(), item.getUsageIntensity(), item.getNextPredictionDate(),
                item.getLastEventAt(), item.getCreatedAt(), item.getUpdatedAt());
    }
}
