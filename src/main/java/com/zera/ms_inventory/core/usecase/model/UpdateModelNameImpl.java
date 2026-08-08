package com.zera.ms_inventory.core.usecase.model;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.exception.ModelNotFoundException;
import com.zera.ms_inventory.core.repository.ModelRepository;

public class UpdateModelNameImpl implements UpdateModelName {
    private final ModelRepository modelRepository;

    public UpdateModelNameImpl(ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
    }

    @Override
    public Model execute(UUID id, String name) {
        Model model = modelRepository.findById(id)
                .orElseThrow(() -> new ModelNotFoundException(id));
        model.rename(name);
        return modelRepository.save(model);
    }
}
