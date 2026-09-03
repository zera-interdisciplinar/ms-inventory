package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.transaction.annotation.Transactional;

import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.CategoryNode;

interface CategoryNeo4jRepository extends Neo4jRepository<CategoryNode, UUID> {

    List<CategoryNode> findAllByUnitId(UUID unitId);

    Optional<CategoryNode> findByIdAndUnitId(UUID id, UUID unitId);

    @Transactional
    void deleteByIdAndUnitId(UUID id, UUID unitId);
}
