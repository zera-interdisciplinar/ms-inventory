package com.zera.ms_inventory.core.usecase.model;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.repository.ModelRepository;

@Service
public class FindAllModelsImpl implements FindAllModels {
    private final ModelRepository modelRepository;

    public FindAllModelsImpl(ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
    }

    @Override
    public List<Model> execute(UUID unitId) {
        return modelRepository.findAll(unitId);
    }
}
