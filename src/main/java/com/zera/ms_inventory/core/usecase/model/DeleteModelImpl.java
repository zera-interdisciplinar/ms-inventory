package com.zera.ms_inventory.core.usecase.model;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.exception.ModelInUseException;
import com.zera.ms_inventory.core.domain.exception.ModelNotFoundException;
import com.zera.ms_inventory.core.repository.ItemRepository;
import com.zera.ms_inventory.core.repository.ModelRepository;

@Service
public class DeleteModelImpl implements DeleteModel {
    private final ModelRepository modelRepository;
    private final ItemRepository itemRepository;

    public DeleteModelImpl(ModelRepository modelRepository, ItemRepository itemRepository) {
        this.modelRepository = modelRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public void execute(UUID unitId, UUID id) {
        modelRepository.findById(unitId, id)
                .orElseThrow(() -> new ModelNotFoundException(id));
        // apagar o modelo deixaria itens sem modelo, sem peso e sem material para os indicadores
        if (itemRepository.existsByModel(unitId, id)) {
            throw new ModelInUseException(id);
        }
        modelRepository.deleteById(unitId, id);
    }
}
