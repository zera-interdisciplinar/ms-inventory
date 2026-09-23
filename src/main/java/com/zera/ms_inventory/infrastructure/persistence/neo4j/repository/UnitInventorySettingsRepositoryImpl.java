package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.zera.ms_inventory.core.domain.entity.UnitInventorySettings;
import com.zera.ms_inventory.core.repository.UnitInventorySettingsRepository;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.UnitInventorySettingsMapper;

@Repository
public class UnitInventorySettingsRepositoryImpl implements UnitInventorySettingsRepository {

    private final UnitInventorySettingsNeo4jRepository neo4jRepository;
    private final UnitInventorySettingsMapper mapper;

    public UnitInventorySettingsRepositoryImpl(UnitInventorySettingsNeo4jRepository neo4jRepository,
                                               UnitInventorySettingsMapper mapper) {
        this.neo4jRepository = neo4jRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<UnitInventorySettings> findByUnit(UUID unitId) {
        return neo4jRepository.findById(unitId).map(mapper::toDomain);
    }

    @Override
    public UnitInventorySettings save(UnitInventorySettings settings) {
        return mapper.toDomain(neo4jRepository.save(mapper.toNode(settings)));
    }
}
