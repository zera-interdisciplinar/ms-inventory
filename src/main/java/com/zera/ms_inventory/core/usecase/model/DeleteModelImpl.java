package com.zera.ms_inventory.core.usecase.model;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.exception.ModelNotFoundException;
import com.zera.ms_inventory.core.repository.ModelRepository;

@Service
public class DeleteModelImpl implements DeleteModel {
    private final ModelRepository modelRepository;

    public DeleteModelImpl(ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
    }

    @Override
    public void execute(UUID unitId, UUID id) {
        modelRepository.findById(unitId, id)
                .orElseThrow(() -> new ModelNotFoundException(id));
        modelRepository.deleteById(unitId, id);
    }
}
