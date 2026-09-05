package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.ModelNode;

interface ModelNeo4jRepository extends Neo4jRepository<ModelNode, UUID> {

    List<ModelNode> findAllByUnitId(UUID unitId);

    Optional<ModelNode> findByIdAndUnitId(UUID id, UUID unitId);

    @Transactional
    void deleteByIdAndUnitId(UUID id, UUID unitId);

    @Query("""
            CALL db.index.vector.queryNodes('model_embeddings', $overFetch, $queryVector)
            YIELD node, score
            WITH node, score
            WHERE node.unitId = $unitId
            ORDER BY score DESC
            LIMIT $limit
            OPTIONAL MATCH (node)-[r:BELONGS_TO]->(c:Category)
            RETURN node, collect(r), collect(c)
            """)
    List<ModelNode> semanticSearch(@Param("queryVector") List<Float> queryVector,
                                   @Param("unitId") UUID unitId,
                                   @Param("overFetch") int overFetch,
                                   @Param("limit") int limit);
}
