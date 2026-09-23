package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.EventNode;

interface EventNeo4jRepository extends Neo4jRepository<EventNode, UUID> {

    @Query("""
            MATCH (e:Event {unitId: $unitId, itemId: $itemId})
            RETURN e
            ORDER BY e.occurredAt DESC, e.id DESC
            SKIP $skip LIMIT $limit
            """)
    List<EventNode> findPageByItem(@Param("unitId") UUID unitId, @Param("itemId") UUID itemId,
                                   @Param("skip") long skip, @Param("limit") int limit);

    @Query("MATCH (e:Event {unitId: $unitId, itemId: $itemId}) RETURN count(e)")
    long countByItem(@Param("unitId") UUID unitId, @Param("itemId") UUID itemId);

    @Query("""
            MATCH (e:Event {unitId: $unitId, itemId: $itemId})
            RETURN e
            ORDER BY e.occurredAt DESC, e.id DESC
            """)
    List<EventNode> findAllByItem(@Param("unitId") UUID unitId, @Param("itemId") UUID itemId);

    /** Liga o evento ao item depois do save; o item nao mapeia a relacao para nao carregar o historico. */
    @Query("""
            MATCH (i:Item {id: $itemId, unitId: $unitId})
            MATCH (e:Event {id: $eventId})
            MERGE (i)-[:HAS_EVENT]->(e)
            """)
    void attachToItem(@Param("unitId") UUID unitId, @Param("itemId") UUID itemId, @Param("eventId") UUID eventId);
}
