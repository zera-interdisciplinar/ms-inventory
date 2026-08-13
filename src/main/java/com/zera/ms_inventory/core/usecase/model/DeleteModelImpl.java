package com.zera.ms_inventory.core.usecase.model;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.exception.ModelNotFoundException;
import com.zera.ms_inventory.core.repository.ModelRepository;

public class DeleteModelImpl implements DeleteModel {
    private final ModelRepository modelRepository;

    public DeleteModelImpl(ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
    }

    @Override
    public void execute(UUID id) {
        modelRepository.findById(id)
                .orElseThrow(() -> new ModelNotFoundException(id));
        modelRepository.deleteById(id);
    }
}
