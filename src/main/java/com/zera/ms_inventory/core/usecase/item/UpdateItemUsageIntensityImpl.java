package com.zera.ms_inventory.core.usecase.item;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.repository.ItemRepository;

@Service
public class UpdateItemUsageIntensityImpl implements UpdateItemUsageIntensity {
    private final ItemRepository itemRepository;

    public UpdateItemUsageIntensityImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public Item execute(UUID unitId, UUID id, Integer usageIntensity) {
        Item item = itemRepository.findById(unitId, id)
                .orElseThrow(() -> new ItemNotFoundException(id));
        item.updateUsageIntensity(usageIntensity);
        return itemRepository.save(item);
    }
}
