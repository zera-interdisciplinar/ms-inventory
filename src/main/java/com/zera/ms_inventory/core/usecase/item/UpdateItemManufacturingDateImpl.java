package com.zera.ms_inventory.core.usecase.item;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.repository.ItemRepository;

public class UpdateItemManufacturingDateImpl implements UpdateItemManufacturingDate {
    private final ItemRepository itemRepository;

    public UpdateItemManufacturingDateImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public Item execute(UUID id, Integer manufacturingDate) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));
        item.updateManufacturingDate(manufacturingDate);
        return itemRepository.save(item);
    }
}
