package com.zera.ms_inventory.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.neo4j.core.Neo4jClient;

class Neo4jIndexInitializerTest {

    private final Neo4jClient client = mock(Neo4jClient.class);

    @Test
    void shouldCreateOneIndexPerScopedLabelAndOnePerEmbeddedLabel() {
        List<String> statements = new Neo4jIndexInitializer(client, 768).statements();

        assertThat(statements).hasSize(5);
        assertThat(statements).anyMatch(s -> s.contains("INDEX item_unit") && s.contains("(i.unitId)"));
        assertThat(statements).anyMatch(s -> s.contains("INDEX model_unit") && s.contains("(m.unitId)"));
        assertThat(statements).anyMatch(s -> s.contains("INDEX category_unit") && s.contains("(c.unitId)"));
    }

    @Test
    void shouldUseConfiguredDimensionsInVectorIndexes() {
        List<String> statements = new Neo4jIndexInitializer(client, 1536).statements();

        assertThat(statements).filteredOn(s -> s.startsWith("CREATE VECTOR INDEX")).hasSize(2)
                .allMatch(s -> s.contains("`vector.dimensions`: 1536"))
                .allMatch(s -> s.contains("`vector.similarity_function`: 'cosine'"));
    }

    @Test
    void shouldKeepBootingWhenAnIndexFailsToCreate() {
        // um indice preexistente com outra config nao pode derrubar o app
        when(client.query(anyString())).thenThrow(new IllegalStateException("index already exists"));

        new Neo4jIndexInitializer(client, 768).createIndexes();

        verify(client, times(5)).query(anyString());
    }
}
