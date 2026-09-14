package com.zera.ms_inventory.core.usecase.model;

import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Category;
import com.zera.ms_inventory.core.domain.entity.Material;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.exception.CategoryNotFoundException;
import com.zera.ms_inventory.core.repository.CategoryRepository;
import com.zera.ms_inventory.core.repository.ModelRepository;
import com.zera.ms_inventory.core.usecase.material.MaterialResolver;

@Service
public class CreateModelImpl implements CreateModel {
    private final ModelRepository modelRepository;
    private final CategoryRepository categoryRepository;
    private final MaterialResolver materialResolver;

    public CreateModelImpl(ModelRepository modelRepository, CategoryRepository categoryRepository,
                           MaterialResolver materialResolver) {
        this.modelRepository = modelRepository;
        this.categoryRepository = categoryRepository;
        this.materialResolver = materialResolver;
    }

    @Override
    public Model execute(CreateModelCommand command) {
        // resolvida pelo par (categoryId, unitId): categoria de outra unidade nao existe daqui
        Category category = categoryRepository.findById(command.unitId(), command.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(command.categoryId()));
        Set<Material> materials = materialResolver.resolve(command.materials());

        Model model = new Model(UUID.randomUUID(), command.unitId(), command.name(), command.manufacturer(),
                command.warrantyMonths(), command.expectedLifespanMonths(), Set.of(), materials,
                command.estimatedWeightKg(), command.notes(), category);
        return modelRepository.save(model);
    }
}
