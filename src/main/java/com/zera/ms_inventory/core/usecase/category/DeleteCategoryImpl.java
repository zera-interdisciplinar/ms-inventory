package com.zera.ms_inventory.core.usecase.category;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.exception.CategoryInUseException;
import com.zera.ms_inventory.core.domain.exception.CategoryNotFoundException;
import com.zera.ms_inventory.core.repository.CategoryRepository;
import com.zera.ms_inventory.core.repository.ModelRepository;

@Service
public class DeleteCategoryImpl implements DeleteCategory {
    private final CategoryRepository categoryRepository;
    private final ModelRepository modelRepository;

    public DeleteCategoryImpl(CategoryRepository categoryRepository, ModelRepository modelRepository) {
        this.categoryRepository = categoryRepository;
        this.modelRepository = modelRepository;
    }

    @Override
    public void execute(UUID unitId, UUID id) {
        categoryRepository.findById(unitId, id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        if (modelRepository.existsByCategory(unitId, id)) {
            throw new CategoryInUseException(id);
        }
        categoryRepository.deleteById(unitId, id);
    }
}
