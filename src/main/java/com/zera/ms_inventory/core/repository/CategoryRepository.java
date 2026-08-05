package com.zera.ms_inventory.core.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Category;

public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(UUID id);
    List<Category> findAll();
    void deleteById(UUID id);
}
