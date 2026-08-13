package com.zera.ms_inventory.core.usecase.item;

import java.time.LocalDate;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.repository.ItemRepository;

public class UpdateItemAcquiredAtImpl implements UpdateItemAcquiredAt {
    private final ItemRepository itemRepository;

    public UpdateItemAcquiredAtImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public Item execute(UUID id, LocalDate acquiredAt) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));
        item.updateAcquiredAt(acquiredAt);
        return itemRepository.save(item);
    }
}
