package com.zera.ms_inventory.core.usecase.model;

import java.util.Set;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.repository.ModelRepository;

public class CreateModelImpl implements CreateModel {
    private final ModelRepository modelRepository;

    public CreateModelImpl(ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
    }

    @Override
    public Model execute(String name, String manufacturer, Integer warrantyMonths, Integer expectedLifespanMonths, Set<String> hazardousMaterials) {
        Model model = new Model(UUID.randomUUID(), name, manufacturer, warrantyMonths, expectedLifespanMonths, hazardousMaterials);
        return modelRepository.save(model);
    }
}
