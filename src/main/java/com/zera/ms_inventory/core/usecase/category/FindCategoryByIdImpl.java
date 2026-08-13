package com.zera.ms_inventory.core.usecase.category;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Category;
import com.zera.ms_inventory.core.domain.exception.CategoryNotFoundException;
import com.zera.ms_inventory.core.repository.CategoryRepository;

@Service
public class FindCategoryByIdImpl implements FindCategoryById {
    private final CategoryRepository categoryRepository;

    public FindCategoryByIdImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Category execute(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }
}
