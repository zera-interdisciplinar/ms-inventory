package com.zera.ms_inventory.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.neo4j.core.Neo4jClient;

class Neo4jMigrationRunnerTest {

    private final Neo4jClient client = mock(Neo4jClient.class);

    private Neo4jMigrationRunner runner(boolean enabled) {
        return spy(new Neo4jMigrationRunner(client, new PathMatchingResourcePatternResolver(), enabled));
    }

    @Test
    void shouldParseVersionDescriptionAndStatementsIgnoringComments() {
        Neo4jMigration migration = Neo4jMigration.parse("V007__add_things.cypher", """
                // comentario; com ponto e virgula
                CREATE CONSTRAINT a IF NOT EXISTS FOR (n:A) REQUIRE n.id IS UNIQUE;

                MATCH (n:A)
                SET n.flag = true;
                """);

        assertThat(migration.version()).isEqualTo(7);
        assertThat(migration.description()).isEqualTo("add things");
        assertThat(migration.statements()).containsExactly(
                "CREATE CONSTRAINT a IF NOT EXISTS FOR (n:A) REQUIRE n.id IS UNIQUE",
                "MATCH (n:A)\nSET n.flag = true");
    }

    @Test
    void shouldRejectInvalidFileNamesAndEmptyMigrations() {
        assertThatThrownBy(() -> Neo4jMigration.parse("add_things.cypher", "RETURN 1;"))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> Neo4jMigration.parse("V1__empty.cypher", "// nada\n"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldLoadTheBundledMigrationsInVersionOrder() {
        List<Neo4jMigration> migrations = runner(true).loadMigrations();

        assertThat(migrations).extracting(Neo4jMigration::version).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9);
        assertThat(migrations.get(0).statements()).singleElement()
                .asString().contains("REQUIRE (i.unitId, i.barcode) IS UNIQUE");
        assertThat(migrations.get(1).statements()).hasSize(2)
                .anyMatch(s -> s.contains("MERGE (m:Material {code: material.code})"));
    }

    @Test
    void shouldApplyOnlyPendingMigrationsInOrderAndRecordThem() {
        Neo4jMigrationRunner runner = runner(true);
        Neo4jMigration first = new Neo4jMigration(1, "first", List.of("RETURN 1"));
        Neo4jMigration second = new Neo4jMigration(2, "second", List.of("RETURN 2", "RETURN 3"));
        doReturn(Set.of(1)).when(runner).appliedVersions();
        doNothing().when(runner).execute(anyString());
        doNothing().when(runner).record(any());

        runner.migrate(List.of(first, second));

        InOrder order = inOrder(runner);
        order.verify(runner).execute("RETURN 2");
        order.verify(runner).execute("RETURN 3");
        order.verify(runner).record(second);
        verify(runner, never()).execute("RETURN 1");
        verify(runner, never()).record(first);
    }

    @Test
    void shouldFailBootAndNotRecordWhenAStatementFails() {
        Neo4jMigrationRunner runner = runner(true);
        Neo4jMigration broken = new Neo4jMigration(3, "broken", List.of("BROKEN"));
        doReturn(Set.of()).when(runner).appliedVersions();
        doThrow(new IllegalStateException("constraint violation")).when(runner).execute("BROKEN");

        assertThatThrownBy(() -> runner.migrate(List.of(broken)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("V3");
        verify(runner, never()).record(any());
    }

    @Test
    void shouldDoNothingWhenDisabled() {
        runner(false).run(null);

        verifyNoInteractions(client);
    }
}
