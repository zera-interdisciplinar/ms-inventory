package com.zera.ms_inventory.core.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Category;

/** Toda leitura e escrita e escopada por unidade: nao existe caminho sem unitId. */
public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(UUID unitId, UUID id);
    List<Category> findAll(UUID unitId);
    void deleteById(UUID unitId, UUID id);
}
