package com.zera.ms_inventory.core.usecase.item;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.exception.ModelNotFoundException;
import com.zera.ms_inventory.core.repository.ItemRepository;
import com.zera.ms_inventory.core.repository.ModelRepository;

@Service
public class CreateItemImpl implements CreateItem {
    private final ItemRepository itemRepository;
    private final ModelRepository modelRepository;

    public CreateItemImpl(ItemRepository itemRepository, ModelRepository modelRepository) {
        this.itemRepository = itemRepository;
        this.modelRepository = modelRepository;
    }

    @Override
    public Item execute(CreateItemCommand command) {
        // resolvido pelo par (modelId, unitId): modelo de outra unidade nao existe daqui
        Model model = modelRepository.findById(command.unitId(), command.modelId())
                .orElseThrow(() -> new ModelNotFoundException(command.modelId()));

        Item item = new Item(UUID.randomUUID(), command.barcode(), command.status(), command.unitId(), model,
                command.nextPredictionDate(), command.manufacturingDate(), command.usageIntensity(),
                command.serialNumber(), command.acquiredAt());
        return itemRepository.save(item);
    }
}
