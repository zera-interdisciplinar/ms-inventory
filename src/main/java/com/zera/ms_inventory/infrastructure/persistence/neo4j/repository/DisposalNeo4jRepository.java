package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.time.LocalDate;
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

    /**
     * Cancelado some da lista: o app filtra o historico visivel, nao o que ja foi desfeito.
     * Parametro nulo nao restringe, no mesmo molde da listagem de itens.
     */
    String FILTER = """
            MATCH (d:Disposal {unitId: $unitId})
            WHERE (d.cancelled IS NULL OR d.cancelled = false)
              AND ($destination IS NULL OR d.destination = $destination)
              AND ($disposedFrom IS NULL OR d.disposedAt >= $disposedFrom)
              AND ($disposedTo IS NULL OR d.disposedAt <= $disposedTo)
              AND ($createdFrom IS NULL OR date(d.createdAt) >= date($createdFrom))
              AND ($createdTo IS NULL OR date(d.createdAt) <= date($createdTo))
              AND ($createdBy IS NULL OR d.createdBy = $createdBy)
              AND ($placeId IS NULL OR d.placeId = $placeId)
              AND ($itemId IS NULL OR EXISTS { MATCH (d)-[:INCLUDES]->(:Item {id: $itemId}) })
              AND ($query IS NULL
                   OR toLower(coalesce(d.placeName, '')) CONTAINS toLower($query)
                   OR toLower(coalesce(d.notes, '')) CONTAINS toLower($query)
                   OR toLower(coalesce(d.createdByName, '')) CONTAINS toLower($query)
                   OR EXISTS { MATCH (d)-[:INCLUDES]->(i:Item)
                               WHERE i.displayCode STARTS WITH $query
                                  OR toLower(coalesce(i.name, '')) CONTAINS toLower($query) })
            """;

    @Query(FILTER + """
            WITH d ORDER BY d.disposedAt DESC, d.createdAt DESC SKIP $skip LIMIT $limit
            OPTIONAL MATCH (d)-[inc:INCLUDES]->(i:Item)
            WITH d, collect(inc) AS incs, collect(i) AS items
            ORDER BY d.disposedAt DESC, d.createdAt DESC
            RETURN d, incs, items
            """)
    List<DisposalNode> findFilteredPage(@Param("unitId") UUID unitId, @Param("destination") String destination,
                                        @Param("disposedFrom") LocalDate disposedFrom,
                                        @Param("disposedTo") LocalDate disposedTo,
                                        @Param("createdFrom") LocalDate createdFrom,
                                        @Param("createdTo") LocalDate createdTo,
                                        @Param("createdBy") UUID createdBy, @Param("placeId") String placeId,
                                        @Param("itemId") UUID itemId, @Param("query") String query,
                                        @Param("skip") long skip, @Param("limit") int limit);

    @Query(FILTER + "RETURN count(d)")
    long countFiltered(@Param("unitId") UUID unitId, @Param("destination") String destination,
                       @Param("disposedFrom") LocalDate disposedFrom, @Param("disposedTo") LocalDate disposedTo,
                       @Param("createdFrom") LocalDate createdFrom, @Param("createdTo") LocalDate createdTo,
                       @Param("createdBy") UUID createdBy, @Param("placeId") String placeId,
                       @Param("itemId") UUID itemId, @Param("query") String query);
}
