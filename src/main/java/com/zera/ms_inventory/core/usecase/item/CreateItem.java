package com.zera.ms_inventory.core.usecase.item;

import com.zera.ms_inventory.core.domain.entity.Item;

public interface CreateItem {
    Item execute(CreateItemCommand command);
}
