package com.zera.ms_inventory.core.usecase.item;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.core.domain.valueobject.DamageType;
import com.zera.ms_inventory.core.domain.valueobject.ItemCondition;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;

public record CreateItemCommand(
        Barcode barcode,
        ItemStatus status,
        UUID unitId,
        UUID modelId,
        LocalDateTime nextPredictionDate,
        Integer manufacturingDate,
        Integer usageIntensity,
        String serialNumber,
        LocalDate acquiredAt,
        String name,
        ItemCondition condition,
        Boolean hasDamages,
        Set<DamageType> damages,
        String notes,
        Actor actor
) {}
