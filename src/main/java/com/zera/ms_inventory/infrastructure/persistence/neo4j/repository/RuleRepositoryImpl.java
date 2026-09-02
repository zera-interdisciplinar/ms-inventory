package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.repository.RuleRepository;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.RuleMapper;

@Repository
public class RuleRepositoryImpl implements RuleRepository {

    private final RuleNeo4jRepository neo4jRepository;
    private final RuleMapper mapper;

    public RuleRepositoryImpl(RuleNeo4jRepository neo4jRepository, RuleMapper mapper) {
        this.neo4jRepository = neo4jRepository;
        this.mapper = mapper;
    }

    @Override
    public Rule save(Rule rule) {
        return mapper.toDomain(neo4jRepository.save(mapper.toNode(rule)));
    }

    @Override
    public Optional<Rule> findById(UUID id) {
        return neo4jRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Rule> findAll() {
        return neo4jRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        neo4jRepository.deleteById(id);
    }
}
