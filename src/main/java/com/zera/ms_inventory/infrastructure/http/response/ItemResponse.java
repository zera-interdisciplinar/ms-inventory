package com.zera.ms_inventory.infrastructure.http.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.DamageType;
import com.zera.ms_inventory.core.domain.valueobject.ItemCondition;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;

public record ItemResponse(
        UUID id,
        String barcode,
        String name,
        ItemStatus status,
        ItemCondition condition,
        Boolean hasDamages,
        Set<DamageType> damages,
        String notes,
        UUID unitId,
        ModelResponse model,
        String serialNumber,
        LocalDate acquiredAt,
        Integer manufacturingDate,
        Integer usageIntensity,
        LocalDateTime nextPredictionDate,
        LocalDateTime lastEventAt,
        UUID createdBy,
        String createdByName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ItemResponse from(Item item) {
        if (item == null) {
            return null;
        }
        return new ItemResponse(item.getId(), item.getBarcode().getValue(), item.getName(), item.getStatus(),
                item.getCondition(), item.getHasDamages(), item.getDamages(), item.getNotes(), item.getUnitId(),
                ModelResponse.from(item.getModel()), item.getSerialNumber(), item.getAcquiredAt(),
                item.getManufacturingDate(), item.getUsageIntensity(), item.getNextPredictionDate(),
                item.getLastEventAt(), item.getCreatedBy(), item.getCreatedByName(), item.getCreatedAt(),
                item.getUpdatedAt());
    }
}
