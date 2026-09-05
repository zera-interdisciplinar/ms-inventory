package com.zera.ms_inventory.core.usecase.item;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Item;

public interface FindItemById {
    Item execute(UUID unitId, UUID id);
}
