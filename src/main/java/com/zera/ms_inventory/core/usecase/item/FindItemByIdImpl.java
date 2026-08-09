package com.zera.ms_inventory.core.usecase.item;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.repository.ItemRepository;

public class FindItemByIdImpl implements FindItemById {
    private final ItemRepository itemRepository;

    public FindItemByIdImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public Item execute(UUID id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));
    }
}
