package com.zera.ms_inventory.core.usecase.item;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.repository.ItemRepository;

public class DeleteItemImpl implements DeleteItem {
    private final ItemRepository itemRepository;

    public DeleteItemImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public void execute(UUID id) {
        itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));
        itemRepository.deleteById(id);
    }
}
