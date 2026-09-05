package com.zera.ms_inventory.core.usecase.item;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.repository.ItemRepository;

@Service
public class UpdateItemAcquiredAtImpl implements UpdateItemAcquiredAt {
    private final ItemRepository itemRepository;

    public UpdateItemAcquiredAtImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public Item execute(UUID unitId, UUID id, LocalDate acquiredAt) {
        Item item = itemRepository.findById(unitId, id)
                .orElseThrow(() -> new ItemNotFoundException(id));
        item.updateAcquiredAt(acquiredAt);
        return itemRepository.save(item);
    }
}
