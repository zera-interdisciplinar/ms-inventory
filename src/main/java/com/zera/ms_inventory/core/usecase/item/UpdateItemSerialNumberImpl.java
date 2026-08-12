package com.zera.ms_inventory.core.usecase.item;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.repository.ItemRepository;

public class UpdateItemSerialNumberImpl implements UpdateItemSerialNumber {
    private final ItemRepository itemRepository;

    public UpdateItemSerialNumberImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public Item execute(UUID id, String serialNumber) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));
        item.updateSerialNumber(serialNumber);
        return itemRepository.save(item);
    }
}
