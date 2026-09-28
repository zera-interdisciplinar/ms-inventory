package com.zera.ms_inventory.core.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Rule;

/** Toda leitura e escrita e escopada por unidade: regra de uma unidade nao vaza para outra. */
public interface RuleRepository {
    Rule save(Rule rule);
    Optional<Rule> findById(UUID unitId, UUID id);
    List<Rule> findAll(UUID unitId);
    long countByUnit(UUID unitId);
    List<Rule> saveAll(List<Rule> rules);
    void deleteById(UUID unitId, UUID id);
}
