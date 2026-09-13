package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.neo4j.repository.Neo4jRepository;

import com.zera.ms_inventory.core.domain.valueobject.MaterialCode;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.MaterialNode;

interface MaterialNeo4jRepository extends Neo4jRepository<MaterialNode, UUID> {

    List<MaterialNode> findAllByOrderByNameAsc();

    Optional<MaterialNode> findByCode(MaterialCode code);

    List<MaterialNode> findAllByCodeIn(Collection<MaterialCode> codes);
}
