package com.zera.ms_inventory.core.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Model;

/** Toda leitura e escrita e escopada por unidade: nao existe caminho sem unitId. */
public interface ModelRepository {
    Model save(Model model);
    Optional<Model> findById(UUID unitId, UUID id);
    List<Model> findAll(UUID unitId);
    List<Model> semanticSearch(UUID unitId, String query, int limit);
    void deleteById(UUID unitId, UUID id);
}
