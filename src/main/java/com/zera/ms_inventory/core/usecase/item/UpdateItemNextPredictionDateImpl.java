package com.zera.ms_inventory.core.usecase.item;

import java.time.LocalDateTime;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.repository.ItemRepository;

public class UpdateItemNextPredictionDateImpl implements UpdateItemNextPredictionDate {
    private final ItemRepository itemRepository;

    public UpdateItemNextPredictionDateImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public Item execute(UUID id, LocalDateTime nextPredictionDate) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));
        item.updateNextPredictionDate(nextPredictionDate);
        return itemRepository.save(item);
    }
}
