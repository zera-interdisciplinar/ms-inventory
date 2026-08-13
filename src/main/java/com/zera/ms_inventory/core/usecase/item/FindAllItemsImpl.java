package com.zera.ms_inventory.core.usecase.item;

import java.util.List;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.repository.ItemRepository;

public class FindAllItemsImpl implements FindAllItems {
    private final ItemRepository itemRepository;

    public FindAllItemsImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public List<Item> execute() {
        return itemRepository.findAll();
    }
}
