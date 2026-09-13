package com.zera.ms_inventory.infrastructure.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

/**
 * Aplica as migracoes versionadas do Neo4j no boot, antes de o app aceitar trafego. Cada versao
 * aplicada vira um no {@code :__Neo4jMigration}; uma migracao que falha derruba o boot, para o
 * servico nunca subir com o schema pela metade.
 */
@Component
public class Neo4jMigrationRunner implements ApplicationRunner {

    static final String LOCATION = "classpath:neo4j/migrations/V*__*.cypher";

    private static final Logger log = LoggerFactory.getLogger(Neo4jMigrationRunner.class);

    private final Neo4jClient neo4jClient;
    private final ResourcePatternResolver resourceResolver;
    private final boolean enabled;

    public Neo4jMigrationRunner(Neo4jClient neo4jClient,
                                ResourcePatternResolver resourceResolver,
                                @Value("${zera.neo4j.migrations.enabled:true}") boolean enabled) {
        this.neo4jClient = neo4jClient;
        this.resourceResolver = resourceResolver;
        this.enabled = enabled;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            log.info("Neo4j migrations disabled (zera.neo4j.migrations.enabled=false)");
            return;
        }
        migrate(loadMigrations());
    }

    void migrate(List<Neo4jMigration> migrations) {
        Set<Integer> applied = appliedVersions();
        for (Neo4jMigration migration : migrations) {
            if (applied.contains(migration.version())) {
                continue;
            }
            log.info("Applying Neo4j migration V{} ({})", migration.version(), migration.description());
            try {
                migration.statements().forEach(this::execute);
            } catch (RuntimeException e) {
                throw new IllegalStateException("Neo4j migration V" + migration.version()
                        + " (" + migration.description() + ") failed", e);
            }
            record(migration);
        }
    }

    List<Neo4jMigration> loadMigrations() {
        List<Neo4jMigration> migrations = new ArrayList<>();
        try {
            for (Resource resource : resourceResolver.getResources(LOCATION)) {
                migrations.add(Neo4jMigration.parse(resource.getFilename(),
                        resource.getContentAsString(StandardCharsets.UTF_8)));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read Neo4j migrations", e);
        }
        migrations.sort(Comparator.comparingInt(Neo4jMigration::version));
        for (int i = 1; i < migrations.size(); i++) {
            if (migrations.get(i).version() == migrations.get(i - 1).version()) {
                throw new IllegalStateException("Duplicate Neo4j migration version V" + migrations.get(i).version());
            }
        }
        return migrations;
    }

    Set<Integer> appliedVersions() {
        Collection<Long> versions = neo4jClient.query("MATCH (m:__Neo4jMigration) RETURN m.version AS version")
                .fetchAs(Long.class)
                .mappedBy((typeSystem, row) -> row.get("version").asLong())
                .all();
        Set<Integer> applied = new HashSet<>();
        versions.forEach(version -> applied.add(version.intValue()));
        return applied;
    }

    void execute(String statement) {
        neo4jClient.query(statement).run();
    }

    void record(Neo4jMigration migration) {
        neo4jClient.query("CREATE (:__Neo4jMigration {version: $version, description: $description, appliedAt: datetime()})")
                .bindAll(Map.of("version", migration.version(), "description", migration.description()))
                .run();
    }
}
