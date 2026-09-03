package com.zera.ms_inventory.core.usecase.model;

import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Category;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.exception.CategoryNotFoundException;
import com.zera.ms_inventory.core.repository.CategoryRepository;
import com.zera.ms_inventory.core.repository.ModelRepository;

@Service
public class CreateModelImpl implements CreateModel {
    private final ModelRepository modelRepository;
    private final CategoryRepository categoryRepository;

    public CreateModelImpl(ModelRepository modelRepository, CategoryRepository categoryRepository) {
        this.modelRepository = modelRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Model execute(UUID unitId, String name, String manufacturer, Integer warrantyMonths,
                          Integer expectedLifespanMonths, Set<String> hazardousMaterials, UUID categoryId) {
        // resolvida pelo par (categoryId, unitId): categoria de outra unidade nao existe daqui
        Category category = categoryRepository.findById(unitId, categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        Model model = new Model(UUID.randomUUID(), unitId, name, manufacturer, warrantyMonths,
                expectedLifespanMonths, Set.copyOf(hazardousMaterials), category);
        return modelRepository.save(model);
    }
}
