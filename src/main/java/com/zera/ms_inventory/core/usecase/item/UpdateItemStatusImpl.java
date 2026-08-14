package com.zera.ms_inventory.core.usecase.item;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.repository.ItemRepository;

@Service
public class UpdateItemStatusImpl implements UpdateItemStatus {
    private final ItemRepository itemRepository;

    public UpdateItemStatusImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public Item execute(UUID id, ItemStatus status) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));
        item.updateStatus(status);
        return itemRepository.save(item);
    }
}
