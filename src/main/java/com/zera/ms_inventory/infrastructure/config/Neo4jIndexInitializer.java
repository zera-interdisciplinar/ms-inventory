package com.zera.ms_inventory.infrastructure.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

@Component
public class Neo4jIndexInitializer {

    private static final Logger log = LoggerFactory.getLogger(Neo4jIndexInitializer.class);

    private final Neo4jClient neo4jClient;
    private final int dimensions;

    public Neo4jIndexInitializer(Neo4jClient neo4jClient,
                                  @Value("${spring.ai.google.genai.embedding.text.options.dimensions:768}") int dimensions) {
        this.neo4jClient = neo4jClient;
        this.dimensions = dimensions;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void createIndexes() {
        for (String statement : statements()) {
            try {
                neo4jClient.query(statement).run();
            } catch (RuntimeException e) {
                // um indice que ja existe com outra config nao deve derrubar o app
                log.warn("Failed to create index: {}", e.getMessage());
            }
        }
    }

    List<String> statements() {
        return List.of(
                "CREATE INDEX item_unit IF NOT EXISTS FOR (i:Item) ON (i.unitId)",
                "CREATE INDEX model_unit IF NOT EXISTS FOR (m:Model) ON (m.unitId)",
                "CREATE INDEX category_unit IF NOT EXISTS FOR (c:Category) ON (c.unitId)",
                vectorIndex("model_embeddings", "m:Model", "m"),
                vectorIndex("category_embeddings", "c:Category", "c"));
    }

    private String vectorIndex(String name, String pattern, String alias) {
        return """
                CREATE VECTOR INDEX %s IF NOT EXISTS
                FOR (%s) ON %s.embedding
                OPTIONS { indexConfig: { `vector.dimensions`: %d, `vector.similarity_function`: 'cosine' } }
                """.formatted(name, pattern, alias, dimensions);
    }
}
