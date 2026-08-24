package com.zera.ms_inventory.core.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Rule;

public interface RuleRepository {
    Rule save(Rule rule);
    Optional<Rule> findById(UUID id);
    List<Rule> findAll();
    void deleteById(UUID id);
}
