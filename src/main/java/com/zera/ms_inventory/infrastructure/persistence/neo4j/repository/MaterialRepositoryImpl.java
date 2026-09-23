package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.zera.ms_inventory.core.domain.entity.Material;
import com.zera.ms_inventory.core.domain.valueobject.MaterialCode;
import com.zera.ms_inventory.core.repository.MaterialRepository;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.MaterialMapper;

@Repository
public class MaterialRepositoryImpl implements MaterialRepository {

    private final MaterialNeo4jRepository neo4jRepository;
    private final MaterialMapper mapper;

    public MaterialRepositoryImpl(MaterialNeo4jRepository neo4jRepository, MaterialMapper mapper) {
        this.neo4jRepository = neo4jRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Material> findAll() {
        return neo4jRepository.findAllByOrderByNameAsc().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Material> findByCode(MaterialCode code) {
        return neo4jRepository.findByCode(code).map(mapper::toDomain);
    }

    @Override
    public List<Material> findAllByCodes(Set<MaterialCode> codes) {
        if (codes.isEmpty()) {
            return List.of();
        }
        return neo4jRepository.findAllByCodeIn(codes).stream().map(mapper::toDomain).toList();
    }
}
