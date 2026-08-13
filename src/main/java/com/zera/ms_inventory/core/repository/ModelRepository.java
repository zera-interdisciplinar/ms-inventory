package com.zera.ms_inventory.core.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Model;

public interface ModelRepository {
    Model save(Model model);
    Optional<Model> findById(UUID id);
    List<Model> findAll();
    void deleteById(UUID id);
}
