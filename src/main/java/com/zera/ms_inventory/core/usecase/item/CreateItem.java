package com.zera.ms_inventory.core.usecase.item;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;

public interface CreateItem {
    Item execute(Barcode barcode, ItemStatus status, UUID unitId, LocalDateTime createdAt, LocalDateTime updatedAt,
                 LocalDateTime lastEventAt, LocalDateTime nextPredictionDate, Integer manufacturingDate,
                 Integer usageIntensity, String serialNumber, LocalDate acquiredAt);
}
