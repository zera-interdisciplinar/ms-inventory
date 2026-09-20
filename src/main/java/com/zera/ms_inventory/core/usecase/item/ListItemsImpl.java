package com.zera.ms_inventory.core.usecase.item;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
import com.zera.ms_inventory.core.repository.ItemRepository;

@Service
public class ListItemsImpl implements ListItems {
    private final ItemRepository itemRepository;

    public ListItemsImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public PageResult<Item> execute(UUID unitId, Pagination pagination) {
        return itemRepository.findPage(unitId, pagination);
    }
}
