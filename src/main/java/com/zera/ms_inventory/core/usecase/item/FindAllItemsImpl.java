package com.zera.ms_inventory.core.usecase.item;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.repository.ItemRepository;

@Service
public class FindAllItemsImpl implements FindAllItems {
    private final ItemRepository itemRepository;

    public FindAllItemsImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public List<Item> execute(UUID unitId) {
        return itemRepository.findAll(unitId);
    }
}
