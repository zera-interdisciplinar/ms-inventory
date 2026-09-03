package com.zera.ms_inventory.core.usecase.item;

import java.util.List;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Item;

public interface FindAllItems {
    List<Item> execute(UUID unitId);
}
