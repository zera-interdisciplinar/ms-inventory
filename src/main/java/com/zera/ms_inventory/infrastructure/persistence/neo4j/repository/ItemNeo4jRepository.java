package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.ItemNode;

interface ItemNeo4jRepository extends Neo4jRepository<ItemNode, UUID> {

    List<ItemNode> findAllByUnitId(UUID unitId);

    Optional<ItemNode> findByIdAndUnitId(UUID id, UUID unitId);

    @Transactional
    void deleteByIdAndUnitId(UUID id, UUID unitId);

    @Query("""
            MATCH (i:Item)-[r:IS_MODEL]->(m:Model)
            WHERE i.unitId = $unitId AND m.id IN $modelIds
            RETURN i, collect(r), collect(m)
            """)
    List<ItemNode> findAllByUnitIdAndModelIdIn(@Param("unitId") UUID unitId,
                                              @Param("modelIds") List<UUID> modelIds);
}
