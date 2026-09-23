package com.zera.ms_inventory.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.neo4j.core.Neo4jClient;

/**
 * Complementa o {@link Neo4jMigrationRunnerTest}: cobre o que fala com o {@link Neo4jClient}
 * (versoes aplicadas, execucao e registro), o carregamento do classpath e os invariantes das
 * migracoes empacotadas, sem depender de quais versoes existem hoje.
 */
class Neo4jMigrationRunnerExecutionTest {

    private final Neo4jClient client = mock(Neo4jClient.class);
    private final Neo4jClient.UnboundRunnableSpec spec = mock(Neo4jClient.UnboundRunnableSpec.class);
    private final ResourcePatternResolver resolver = mock(ResourcePatternResolver.class);

    private Neo4jMigrationRunner runner() {
        return new Neo4jMigrationRunner(client, resolver, true);
    }

    private Neo4jMigrationRunner spiedRunner() {
        return spy(runner());
    }

    private static Resource cypher(String fileName, String content) {
        return new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };
    }

    // ---- integracao com o Neo4jClient ----

    @Test
    @SuppressWarnings("unchecked")
    void shouldReadTheAppliedVersionsFromTheMigrationNodes() {
        Neo4jClient.MappingSpec<Long> mapping = mock(Neo4jClient.MappingSpec.class);
        Neo4jClient.RecordFetchSpec<Long> fetch = mock(Neo4jClient.RecordFetchSpec.class);
        when(client.query(anyString())).thenReturn(spec);
        when(spec.fetchAs(Long.class)).thenReturn(mapping);
        when(mapping.mappedBy(any())).thenReturn(fetch);
        when(fetch.all()).thenReturn(List.of(1L, 3L));

        assertThat(runner().appliedVersions()).containsExactlyInAnyOrder(1, 3);

        verify(client).query(contains("(m:__Neo4jMigration)"));
        ArgumentCaptor<BiFunction<TypeSystem, Record, Long>> rowMapper = ArgumentCaptor.forClass(BiFunction.class);
        verify(mapping).mappedBy(rowMapper.capture());
        Record row = mock(Record.class);
        Value version = mock(Value.class);
        when(row.get("version")).thenReturn(version);
        when(version.asLong()).thenReturn(7L);
        assertThat(rowMapper.getValue().apply(null, row)).isEqualTo(7L);
    }

    @Test
    void shouldReportNoAppliedVersionsOnAFreshDatabase() {
        @SuppressWarnings("unchecked")
        Neo4jClient.MappingSpec<Long> mapping = mock(Neo4jClient.MappingSpec.class);
        @SuppressWarnings("unchecked")
        Neo4jClient.RecordFetchSpec<Long> fetch = mock(Neo4jClient.RecordFetchSpec.class);
        when(client.query(anyString())).thenReturn(spec);
        when(spec.fetchAs(Long.class)).thenReturn(mapping);
        when(mapping.mappedBy(any())).thenReturn(fetch);
        when(fetch.all()).thenReturn(List.of());

        assertThat(runner().appliedVersions()).isEmpty();
    }

    @Test
    void shouldRunEachStatementOnTheClient() {
        when(client.query("CREATE CONSTRAINT x IF NOT EXISTS FOR (n:A) REQUIRE n.id IS UNIQUE")).thenReturn(spec);

        runner().execute("CREATE CONSTRAINT x IF NOT EXISTS FOR (n:A) REQUIRE n.id IS UNIQUE");

        verify(spec).run();
    }

    @Test
    void shouldRecordTheAppliedMigrationWithVersionAndDescription() {
        Neo4jClient.RunnableSpec bound = mock(Neo4jClient.RunnableSpec.class);
        when(client.query(anyString())).thenReturn(spec);
        when(spec.bindAll(anyMap())).thenReturn(bound);

        runner().record(new Neo4jMigration(4, "add things", List.of("RETURN 1")));

        verify(client).query(contains("CREATE (:__Neo4jMigration"));
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> params = ArgumentCaptor.forClass(Map.class);
        verify(spec).bindAll(params.capture());
        assertThat(params.getValue()).containsEntry("version", 4).containsEntry("description", "add things");
        verify(bound).run();
    }

    // ---- migrate / run ----

    @Test
    void shouldApplyTheLoadedMigrationsOnRunWhenEnabled() {
        Neo4jMigrationRunner runner = spiedRunner();
        List<Neo4jMigration> migrations = List.of(new Neo4jMigration(1, "first", List.of("RETURN 1")));
        doReturn(migrations).when(runner).loadMigrations();
        doNothing().when(runner).migrate(migrations);

        runner.run(null);

        verify(runner).migrate(migrations);
    }

    @Test
    void shouldTouchNothingWhenEveryMigrationIsAlreadyApplied() {
        Neo4jMigrationRunner runner = spiedRunner();
        doReturn(Set.of(1, 2)).when(runner).appliedVersions();

        runner.migrate(List.of(
                new Neo4jMigration(1, "first", List.of("RETURN 1")),
                new Neo4jMigration(2, "second", List.of("RETURN 2"))));

        verify(runner, never()).execute(anyString());
        verify(runner, never()).record(any());
    }

    @Test
    void shouldStopAtTheFirstFailureKeepingEarlierMigrationsAndSkippingLaterOnes() {
        Neo4jMigrationRunner runner = spiedRunner();
        Neo4jMigration ok = new Neo4jMigration(1, "ok", List.of("RETURN 1"));
        Neo4jMigration broken = new Neo4jMigration(2, "broken one", List.of("RETURN 2", "BROKEN"));
        Neo4jMigration later = new Neo4jMigration(3, "later", List.of("RETURN 3"));
        IllegalStateException failure = new IllegalStateException("constraint violation");
        doReturn(Set.of()).when(runner).appliedVersions();
        doNothing().when(runner).execute(anyString());
        doNothing().when(runner).record(any());
        doThrow(failure).when(runner).execute("BROKEN");

        assertThatThrownBy(() -> runner.migrate(List.of(ok, broken, later)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("V2 (broken one)")
                .hasCause(failure);

        verify(runner).record(ok);
        verify(runner).execute("RETURN 2");
        verify(runner, never()).record(broken);
        verify(runner, never()).execute("RETURN 3");
        verify(runner, never()).record(later);
    }

    // ---- carregamento do classpath ----

    @Test
    void shouldLoadMigrationsSortedByVersionRegardlessOfClasspathOrder() throws IOException {
        when(resolver.getResources(anyString())).thenReturn(new Resource[] {
                cypher("V3__third.cypher", "RETURN 3;"),
                cypher("V1__first.cypher", "RETURN 1;"),
                cypher("V2__second.cypher", "RETURN 2;")});

        assertThat(runner().loadMigrations()).extracting(Neo4jMigration::version).containsExactly(1, 2, 3);
    }

    @Test
    void shouldRejectTwoMigrationsWithTheSameVersion() throws IOException {
        when(resolver.getResources(anyString())).thenReturn(new Resource[] {
                cypher("V1__first.cypher", "RETURN 1;"),
                cypher("V001__again.cypher", "RETURN 2;")});

        assertThatThrownBy(() -> runner().loadMigrations())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate Neo4j migration version V1");
    }

    @Test
    void shouldFailWhenTheMigrationsCannotBeRead() throws IOException {
        IOException failure = new IOException("disk error");
        when(resolver.getResources(anyString())).thenThrow(failure);

        assertThatThrownBy(() -> runner().loadMigrations())
                .isInstanceOf(UncheckedIOException.class)
                .hasCause(failure);
    }

    // ---- migracoes empacotadas ----

    @Test
    void shouldBundleContiguousVersionsStartingAtOne() {
        List<Neo4jMigration> bundled = bundled();

        assertThat(bundled).isNotEmpty();
        assertThat(bundled).extracting(Neo4jMigration::version)
                .containsExactlyElementsOf(IntStream.rangeClosed(1, bundled.size()).boxed().toList());
    }

    /** O split e por ';': um ';' dentro de uma string deixaria aspas abertas no statement cortado. */
    @Test
    void shouldNotSplitStringLiteralsOfTheBundledMigrations() {
        for (Neo4jMigration migration : bundled()) {
            assertThat(migration.statements())
                    .as("statements de V%d (%s)", migration.version(), migration.description())
                    .allSatisfy(statement -> assertThat(endsInsideStringLiteral(statement))
                            .as("statement com string cortada: %s", statement).isFalse());
        }
    }

    private static List<Neo4jMigration> bundled() {
        return new Neo4jMigrationRunner(mock(Neo4jClient.class), new PathMatchingResourcePatternResolver(), true)
                .loadMigrations();
    }

    private static boolean endsInsideStringLiteral(String statement) {
        char quote = 0;
        for (int i = 0; i < statement.length(); i++) {
            char c = statement.charAt(i);
            if (quote != 0 && c == '\\') {
                i++;
            } else if (quote == 0 && (c == '\'' || c == '"')) {
                quote = c;
            } else if (c == quote) {
                quote = 0;
            }
        }
        return quote != 0;
    }
}
