package com.zera.ms_inventory.core.usecase.model;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.exception.ModelNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
import com.zera.ms_inventory.core.repository.ItemRepository;
import com.zera.ms_inventory.core.repository.ModelRepository;

@Service
public class ListModelItemsImpl implements ListModelItems {
    private final ModelRepository modelRepository;
    private final ItemRepository itemRepository;

    public ListModelItemsImpl(ModelRepository modelRepository, ItemRepository itemRepository) {
        this.modelRepository = modelRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public PageResult<Item> execute(UUID unitId, UUID modelId, Pagination pagination) {
        // modelo de outra unidade (ou inexistente) responde 404, nunca uma lista vazia enganosa
        modelRepository.findById(unitId, modelId).orElseThrow(() -> new ModelNotFoundException(modelId));
        return itemRepository.findPageByModel(unitId, modelId, pagination);
    }
}
