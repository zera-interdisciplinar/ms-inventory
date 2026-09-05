package com.zera.ms_inventory.core.usecase.item;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.repository.ItemRepository;

@Service
public class UpdateItemNextPredictionDateImpl implements UpdateItemNextPredictionDate {
    private final ItemRepository itemRepository;

    public UpdateItemNextPredictionDateImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public Item execute(UUID unitId, UUID id, LocalDateTime nextPredictionDate) {
        Item item = itemRepository.findById(unitId, id)
                .orElseThrow(() -> new ItemNotFoundException(id));
        item.updateNextPredictionDate(nextPredictionDate);
        return itemRepository.save(item);
    }
}
