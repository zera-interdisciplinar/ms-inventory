package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.ItemNode;

interface ItemNeo4jRepository extends Neo4jRepository<ItemNode, UUID> {

    List<ItemNode> findAllByUnitId(UUID unitId);

    Page<ItemNode> findAllByUnitId(UUID unitId, Pageable pageable);

    Page<ItemNode> findAllByUnitIdAndModelId(UUID unitId, UUID modelId, Pageable pageable);

    boolean existsByUnitIdAndModelId(UUID unitId, UUID modelId);

    Optional<ItemNode> findByIdAndUnitId(UUID id, UUID unitId);

    @Transactional
    void deleteByIdAndUnitId(UUID id, UUID unitId);

    @Query("""
            MATCH (i:Item)-[r:IS_MODEL]->(m:Model)
            WHERE i.unitId = $unitId AND m.id IN $modelIds
            OPTIONAL MATCH (m)-[bt:BELONGS_TO]->(c:Category)
            OPTIONAL MATCH (m)-[mo:MADE_OF]->(mat:Material)
            RETURN i, collect(DISTINCT r), collect(DISTINCT m), collect(DISTINCT bt), collect(DISTINCT c),
                   collect(DISTINCT mo), collect(DISTINCT mat)
            """)
    List<ItemNode> findAllByUnitIdAndModelIdIn(@Param("unitId") UUID unitId,
                                              @Param("modelIds") List<UUID> modelIds);
}
