package com.zera.ms_inventory.core.usecase.category;

import java.util.List;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Category;
import com.zera.ms_inventory.core.repository.CategoryRepository;

@Service
public class FindAllCategoriesImpl implements FindAllCategories {
    private final CategoryRepository categoryRepository;

    public FindAllCategoriesImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> execute() {
        return categoryRepository.findAll();
    }
}
