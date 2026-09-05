package com.zera.ms_inventory.core.usecase.item;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.valueobject.Barcode;
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
        LocalDate acquiredAt
) {}
