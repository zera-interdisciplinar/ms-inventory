package com.zera.ms_inventory.infrastructure.http.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.DamageType;
import com.zera.ms_inventory.core.domain.valueobject.ItemCondition;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.domain.valueobject.UsageIntensity;

public record ItemResponse(
        UUID id,
        String barcode,
        String displayCode,
        String name,
        ItemStatus status,
        ItemCondition condition,
        Boolean hasDamages,
        Set<DamageType> damages,
        String notes,
        String photoUrl,
        UUID unitId,
        ModelResponse model,
        String serialNumber,
        LocalDate acquiredAt,
        Integer manufacturingYear,
        UsageIntensity usageIntensity,
        LocalDate predictedFailureDate,
        LocalDateTime predictionUpdatedAt,
        LocalDateTime lastEventAt,
        UUID createdBy,
        String createdByName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /** photoUrl ja resolvida pelo {@link ItemResponses}; nula quando o item nao tem foto servivel. */
    public static ItemResponse from(Item item, String photoUrl) {
        if (item == null) {
            return null;
        }
        return new ItemResponse(item.getId(), item.getBarcode().getValue(), item.getDisplayCode(), item.getName(), item.getStatus(),
                item.getCondition(), item.getHasDamages(), item.getDamages(), item.getNotes(), photoUrl, item.getUnitId(),
                ModelResponse.from(item.getModel()), item.getSerialNumber(), item.getAcquiredAt(),
                item.getManufacturingYear(), item.getUsageIntensity(), item.getPredictedFailureDate(),
                item.getPredictionUpdatedAt(),
                item.getLastEventAt(), item.getCreatedBy(), item.getCreatedByName(), item.getCreatedAt(),
                item.getUpdatedAt());
    }
}
