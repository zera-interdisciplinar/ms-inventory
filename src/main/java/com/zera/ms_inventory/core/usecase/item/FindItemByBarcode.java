package com.zera.ms_inventory.core.usecase.item;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Item;

public interface FindItemByBarcode {
    Item execute(UUID unitId, String barcode);
}
