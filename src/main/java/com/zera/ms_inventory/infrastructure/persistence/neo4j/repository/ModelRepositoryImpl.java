package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.repository.ModelRepository;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.ModelMapper;

@Repository
public class ModelRepositoryImpl implements ModelRepository {

    private final ModelNeo4jRepository neo4jRepository;
    private final ModelMapper mapper;

    public ModelRepositoryImpl(ModelNeo4jRepository neo4jRepository, ModelMapper mapper) {
        this.neo4jRepository = neo4jRepository;
        this.mapper = mapper;
    }

    @Override
    public Model save(Model model) {
        return mapper.toDomain(neo4jRepository.save(mapper.toNode(model)));
    }

    @Override
    public Optional<Model> findById(UUID id) {
        return neo4jRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Model> findAll() {
        return neo4jRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        neo4jRepository.deleteById(id);
    }
}
