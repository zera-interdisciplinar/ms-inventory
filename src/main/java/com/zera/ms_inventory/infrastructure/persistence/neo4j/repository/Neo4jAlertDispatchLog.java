package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import com.zera.ms_inventory.core.repository.AlertDispatchLog;

@Repository
public class Neo4jAlertDispatchLog implements AlertDispatchLog {

    private final Neo4jClient neo4jClient;

    public Neo4jAlertDispatchLog(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    /** Um registro por (unidade, regra, assunto); o MERGE por id o mantem unico. */
    static String idOf(UUID unitId, UUID ruleId, UUID subjectId) {
        return unitId + ":" + ruleId + ":" + subjectId;
    }

    @Override
    public boolean sentSince(UUID unitId, UUID ruleId, UUID subjectId, LocalDateTime since) {
        return neo4jClient.query("""
                        MATCH (d:AlertDispatch {id: $id})
                        WHERE d.sentAt > $since
                        RETURN count(d) AS total
                        """)
                .bindAll(Map.of("id", idOf(unitId, ruleId, subjectId), "since", since))
                .fetchAs(Long.class)
                .mappedBy((typeSystem, row) -> row.get("total").asLong())
                .one()
                .orElse(0L) > 0;
    }

    @Override
    public void recordSent(UUID unitId, UUID ruleId, UUID subjectId, LocalDateTime sentAt) {
        neo4jClient.query("""
                        MERGE (d:AlertDispatch {id: $id})
                        SET d.unitId = $unitId, d.ruleId = $ruleId, d.subjectId = $subjectId,
                            d.sentAt = $sentAt
                        """)
                .bindAll(Map.of("id", idOf(unitId, ruleId, subjectId), "unitId", unitId.toString(),
                        "ruleId", ruleId.toString(), "subjectId", subjectId.toString(), "sentAt", sentAt))
                .run();
    }
}
