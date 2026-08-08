package com.zera.ms_inventory.core.usecase.item;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Item;

public interface UpdateItemUsageIntensity {
    Item execute(UUID id, Integer usageIntensity);
}
