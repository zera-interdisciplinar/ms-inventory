package com.zera.ms_inventory.core.usecase.item;

public interface CreateItem {
    CreateItemResult execute(CreateItemCommand command);
}
