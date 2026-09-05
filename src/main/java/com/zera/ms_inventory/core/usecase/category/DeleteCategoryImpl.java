package com.zera.ms_inventory.core.usecase.category;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.exception.CategoryNotFoundException;
import com.zera.ms_inventory.core.repository.CategoryRepository;

@Service
public class DeleteCategoryImpl implements DeleteCategory {
    private final CategoryRepository categoryRepository;

    public DeleteCategoryImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void execute(UUID unitId, UUID id) {
        categoryRepository.findById(unitId, id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        categoryRepository.deleteById(unitId, id);
    }
}
