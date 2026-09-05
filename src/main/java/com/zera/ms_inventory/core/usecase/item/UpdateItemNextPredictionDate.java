package com.zera.ms_inventory.core.usecase.item;

import java.time.LocalDateTime;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Item;

public interface UpdateItemNextPredictionDate {
    Item execute(UUID unitId, UUID id, LocalDateTime nextPredictionDate);
}
