package com.zera.ms_inventory.core.usecase.category;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Category;
import com.zera.ms_inventory.core.domain.exception.CategoryNotFoundException;
import com.zera.ms_inventory.core.repository.CategoryRepository;

@Service
public class UpdateCategoryNameImpl implements UpdateCategoryName {
    private final CategoryRepository categoryRepository;

    public UpdateCategoryNameImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Category execute(UUID unitId, UUID id, String name) {
        Category category = categoryRepository.findById(unitId, id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        category.rename(name);
        return categoryRepository.save(category);
    }
}
