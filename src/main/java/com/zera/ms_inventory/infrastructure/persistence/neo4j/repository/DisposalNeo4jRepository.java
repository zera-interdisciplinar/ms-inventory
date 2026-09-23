package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.DisposalNode;

interface DisposalNeo4jRepository extends Neo4jRepository<DisposalNode, UUID> {

    @Query("""
            MATCH (d:Disposal {id: $id, unitId: $unitId})
            OPTIONAL MATCH (d)-[inc:INCLUDES]->(i:Item)
            RETURN d, collect(inc), collect(i)
            """)
    Optional<DisposalNode> findByIdAndUnitId(@Param("id") UUID id, @Param("unitId") UUID unitId);

    @Query("""
            MATCH (d:Disposal {unitId: $unitId})
            WITH d ORDER BY d.disposedAt DESC, d.createdAt DESC SKIP $skip LIMIT $limit
            OPTIONAL MATCH (d)-[inc:INCLUDES]->(i:Item)
            WITH d, collect(inc) AS incs, collect(i) AS items
            ORDER BY d.disposedAt DESC, d.createdAt DESC
            RETURN d, incs, items
            """)
    List<DisposalNode> findPageByUnit(@Param("unitId") UUID unitId, @Param("skip") long skip,
                                      @Param("limit") int limit);

    @Query("MATCH (d:Disposal {unitId: $unitId}) RETURN count(d)")
    long countByUnit(@Param("unitId") UUID unitId);
}
