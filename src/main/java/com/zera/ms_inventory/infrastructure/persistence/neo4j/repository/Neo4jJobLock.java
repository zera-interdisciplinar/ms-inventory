package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import com.zera.ms_inventory.core.repository.JobLock;

/**
 * Trava por constraint: o no da execucao tem id unico, entao a segunda replica que tentar criar o
 * mesmo par (job, janela) esbarra na constraint e desiste. Sem isso, subir de uma para duas
 * replicas faria o job rodar duas vezes e os alertas sairem em dobro.
 */
@Repository
public class Neo4jJobLock implements JobLock {

    private final Neo4jClient neo4jClient;

    public Neo4jJobLock(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    @Override
    public boolean acquire(String jobName, String window) {
        try {
            return neo4jClient.query("""
                            CREATE (r:JobRun {id: $id, job: $job, window: $window, startedAt: localdatetime()})
                            RETURN r.id AS id
                            """)
                    .bindAll(Map.of("id", jobName + ":" + window, "job", jobName, "window", window))
                    .fetchAs(String.class)
                    .mappedBy((typeSystem, row) -> row.get("id").asString())
                    .one()
                    .isPresent();
        } catch (DataIntegrityViolationException alreadyRunning) {
            return false;
        }
    }
}
