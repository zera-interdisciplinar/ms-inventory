package com.zera.ms_inventory.core.usecase.category;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Category;
import com.zera.ms_inventory.core.repository.CategoryRepository;

@Service
public class CreateCategoryImpl implements CreateCategory {
    private final CategoryRepository categoryRepository;

    public CreateCategoryImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Category execute(UUID unitId, String name, String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        Category category = new Category(UUID.randomUUID(), unitId, name, description, createdAt, updatedAt);
        return categoryRepository.save(category);
    }
}
