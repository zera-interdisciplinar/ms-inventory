package com.zera.ms_inventory.core.usecase.item;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.repository.ItemRepository;

@Service
public class DeleteItemImpl implements DeleteItem {
    private final ItemRepository itemRepository;

    public DeleteItemImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public void execute(UUID unitId, UUID id) {
        itemRepository.findById(unitId, id)
                .orElseThrow(() -> new ItemNotFoundException(id));
        itemRepository.deleteById(unitId, id);
    }
}
