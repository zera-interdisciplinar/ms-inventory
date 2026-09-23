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

    @Query("MATCH (i:Item {unitId: $unitId}) WHERE i.status <> 'REMOVED' RETURN i")
    List<ItemNode> findAllByUnitId(@Param("unitId") UUID unitId);

    String FILTER = """
            MATCH (i:Item)-[r:IS_MODEL]->(m:Model)
            WHERE i.unitId = $unitId
              AND ($status IS NULL OR i.status = $status)
              AND ($status = 'REMOVED' OR i.status <> 'REMOVED')
              AND (NOT $eligibleForDisposal OR i.status IN $disposableStatuses)
              AND ($createdBy IS NULL OR i.createdBy = $createdBy)
              AND ($condition IS NULL OR i.condition = $condition)
              AND ($modelId IS NULL OR m.id = $modelId)
              AND ($categoryId IS NULL OR EXISTS { (m)-[:BELONGS_TO]->(:Category {id: $categoryId}) })
              AND ($query IS NULL
                   OR i.displayCode STARTS WITH $query
                   OR toLower(coalesce(i.name, '')) CONTAINS toLower($query)
                   OR toLower(m.name) CONTAINS toLower($query)
                   OR EXISTS { (m)-[:MADE_OF]->(filterMaterial:Material)
                               WHERE toLower(filterMaterial.name) CONTAINS toLower($query) })
            """;

    /** Pagina filtrada, mais recentes primeiro, com modelo, categoria e materiais carregados. */
    @Query(FILTER + """
            WITH i, r, m ORDER BY i.createdAt DESC SKIP $skip LIMIT $limit
            OPTIONAL MATCH (m)-[bt:BELONGS_TO]->(c:Category)
            OPTIONAL MATCH (m)-[mo:MADE_OF]->(mat:Material)
            WITH i, r, m, collect(DISTINCT bt) AS bts, collect(DISTINCT c) AS cs,
                 collect(DISTINCT mo) AS mos, collect(DISTINCT mat) AS mats
            ORDER BY i.createdAt DESC
            RETURN i, collect(r), collect(m), bts, cs, mos, mats
            """)
    List<ItemNode> findFilteredPage(@Param("unitId") UUID unitId, @Param("status") String status,
                                    @Param("categoryId") UUID categoryId, @Param("modelId") UUID modelId,
                                    @Param("query") String query,
                                    @Param("eligibleForDisposal") boolean eligibleForDisposal,
                                    @Param("disposableStatuses") List<String> disposableStatuses,
                                    @Param("createdBy") UUID createdBy, @Param("condition") String condition,
                                    @Param("skip") long skip, @Param("limit") int limit);

    @Query(FILTER + "RETURN count(DISTINCT i)")
    long countFiltered(@Param("unitId") UUID unitId, @Param("status") String status,
                       @Param("categoryId") UUID categoryId, @Param("modelId") UUID modelId,
                       @Param("query") String query,
                       @Param("eligibleForDisposal") boolean eligibleForDisposal,
                       @Param("disposableStatuses") List<String> disposableStatuses,
                       @Param("createdBy") UUID createdBy, @Param("condition") String condition);

    Page<ItemNode> findAllByUnitIdAndModelId(UUID unitId, UUID modelId, Pageable pageable);

    boolean existsByUnitIdAndModelId(UUID unitId, UUID modelId);

    @Query("MATCH (i:Item {unitId: $unitId})-[:IS_MODEL]->(:Model {id: $modelId}) RETURN count(i)")
    long countByUnitIdAndModelId(@Param("unitId") UUID unitId, @Param("modelId") UUID modelId);

    boolean existsByUnitIdAndDisplayCode(UUID unitId, String displayCode);

    Optional<ItemNode> findByUnitIdAndBarcode(UUID unitId, String barcode);

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
