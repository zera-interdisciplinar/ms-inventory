package com.zera.ms_inventory.core.usecase.item;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Item;

public interface UpdateItemManufacturingDate {
    Item execute(UUID unitId, UUID id, Integer manufacturingDate);
}
