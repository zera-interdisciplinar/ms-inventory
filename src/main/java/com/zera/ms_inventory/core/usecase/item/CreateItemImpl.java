package com.zera.ms_inventory.core.usecase.item;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.repository.ItemRepository;

@Service
public class CreateItemImpl implements CreateItem {
    private final ItemRepository itemRepository;

    public CreateItemImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public Item execute(CreateItemCommand command) {
        Item item = new Item(UUID.randomUUID(), command.barcode(), command.status(), command.unitId(),
                command.nextPredictionDate(), command.manufacturingDate(), command.usageIntensity(),
                command.serialNumber(), command.acquiredAt());
        return itemRepository.save(item);
    }
}
