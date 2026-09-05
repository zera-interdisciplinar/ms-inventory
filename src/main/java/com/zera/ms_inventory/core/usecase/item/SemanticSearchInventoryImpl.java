package com.zera.ms_inventory.core.usecase.item;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.repository.ItemRepository;
import com.zera.ms_inventory.core.repository.ModelRepository;

@Service
public class SemanticSearchInventoryImpl implements SemanticSearchInventory {
    private final ModelRepository modelRepository;
    private final ItemRepository itemRepository;

    public SemanticSearchInventoryImpl(ModelRepository modelRepository, ItemRepository itemRepository) {
        this.modelRepository = modelRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public List<Item> execute(UUID unitId, String query, int limit) {
        List<UUID> modelIds = modelRepository.semanticSearch(unitId, query, limit)
                .stream().map(Model::getId).toList();
        return itemRepository.findAllByModelIds(unitId, modelIds);
    }
}
