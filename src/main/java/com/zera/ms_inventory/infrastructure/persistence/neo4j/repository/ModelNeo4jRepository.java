package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.ModelNode;

interface ModelNeo4jRepository extends Neo4jRepository<ModelNode, UUID> {

    List<ModelNode> findAllByUnitId(UUID unitId);

    Page<ModelNode> findAllByUnitId(UUID unitId, Pageable pageable);

    Optional<ModelNode> findByIdAndUnitId(UUID id, UUID unitId);

    @Transactional
    void deleteByIdAndUnitId(UUID id, UUID unitId);

    /** O SDN nao remove relacoes obsoletas ao salvar uma instancia nova vinda do mapper. */
    @Transactional
    @Query("""
            MATCH (m:Model {id: $id, unitId: $unitId})-[r:MADE_OF]->(material:Material)
            WHERE NOT material.code IN $codes
            DELETE r
            """)
    void removeMaterialsNotIn(@Param("id") UUID id, @Param("unitId") UUID unitId,
                              @Param("codes") Collection<String> codes);

    @Query("""
            CALL db.index.vector.queryNodes('model_embeddings', $overFetch, $queryVector)
            YIELD node, score
            WITH node, score
            WHERE node.unitId = $unitId
            ORDER BY score DESC
            LIMIT $limit
            OPTIONAL MATCH (node)-[r:BELONGS_TO]->(c:Category)
            OPTIONAL MATCH (node)-[mo:MADE_OF]->(mat:Material)
            RETURN node, collect(DISTINCT r), collect(DISTINCT c), collect(DISTINCT mo), collect(DISTINCT mat)
            """)
    List<ModelNode> semanticSearch(@Param("queryVector") List<Float> queryVector,
                                   @Param("unitId") UUID unitId,
                                   @Param("overFetch") int overFetch,
                                   @Param("limit") int limit);
}
