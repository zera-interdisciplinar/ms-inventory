package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neo4j.driver.Record;
import org.neo4j.driver.Values;
import org.springframework.data.neo4j.core.Neo4jClient;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.valueobject.CategoryItemCount;
import com.zera.ms_inventory.core.domain.valueobject.InventoryCounts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardRepositoryImplTest {

    @Mock private Neo4jClient neo4jClient;
    @Mock private Neo4jClient.UnboundRunnableSpec spec;

    private DashboardRepositoryImpl repository() {
        return new DashboardRepositoryImpl(neo4jClient);
    }

    @SuppressWarnings("unchecked")
    private <T> void stubFetchAs(Class<T> tipo, Optional<T> resultado) {
        Neo4jClient.MappingSpec<T> mapping = mock(Neo4jClient.MappingSpec.class);
        Neo4jClient.RecordFetchSpec<T> fetch = mock(Neo4jClient.RecordFetchSpec.class);
        when(neo4jClient.query(anyString())).thenReturn(spec);
        when(spec.bindAll(anyMap())).thenReturn(spec);
        when(spec.fetchAs(tipo)).thenReturn(mapping);
        when(mapping.mappedBy(any())).thenReturn(fetch);
        when(fetch.one()).thenReturn(resultado);
    }

    // ---- mapeamento das linhas ----

    @Test
    void shouldMapTheCountsRow() {
        Record linha = mock(Record.class);
        when(linha.get("activeItems")).thenReturn(Values.value(120L));
        when(linha.get("activeItemsBefore")).thenReturn(Values.value(100L));
        when(linha.get("pendingApproval")).thenReturn(Values.value(3L));
        when(linha.get("inMaintenance")).thenReturn(Values.value(2L));
        when(linha.get("awaitingEvaluation")).thenReturn(Values.value(1L));
        when(linha.get("draft")).thenReturn(Values.value(4L));
        when(linha.get("rejected")).thenReturn(Values.value(5L));

        InventoryCounts contagens = DashboardRepositoryImpl.toCounts(null, linha);

        assertThat(contagens.activeItems()).isEqualTo(120);
        assertThat(contagens.activeItemsBefore()).isEqualTo(100);
        assertThat(contagens.pendingApproval()).isEqualTo(3);
        assertThat(contagens.inMaintenance()).isEqualTo(2);
        assertThat(contagens.awaitingEvaluation()).isEqualTo(1);
        assertThat(contagens.draft()).isEqualTo(4);
        assertThat(contagens.rejected()).isEqualTo(5);
    }

    @Test
    void shouldMapTheTotalRow() {
        Record linha = mock(Record.class);
        when(linha.get("total")).thenReturn(Values.value(7L));

        assertThat(DashboardRepositoryImpl.toTotal(null, linha)).isEqualTo(7L);
    }

    // ---- consultas ----

    @Test
    void shouldReturnTheCountsOfTheUnit() {
        InventoryCounts esperado = new InventoryCounts(10, 8, 1, 1, 1, 1, 1);
        stubFetchAs(InventoryCounts.class, Optional.of(esperado));

        assertThat(repository().countsOf(Fixtures.UNIT, LocalDateTime.now())).isSameAs(esperado);
    }

    /** Unidade sem nenhum item nao devolve linha; o painel abre zerado em vez de quebrar. */
    @Test
    void shouldFallBackToZeroedCountsWithoutRows() {
        stubFetchAs(InventoryCounts.class, Optional.empty());

        assertThat(repository().countsOf(Fixtures.UNIT, LocalDateTime.now()).activeItems()).isZero();
    }

    @Test
    void shouldCountTheDisposalsSinceTheDate() {
        stubFetchAs(Long.class, Optional.of(4L));

        assertThat(repository().countDisposalsSince(Fixtures.UNIT, LocalDate.now().minusDays(30)))
                .isEqualTo(4L);
    }

    @Test
    void shouldReturnZeroDisposalsWithoutRows() {
        stubFetchAs(Long.class, Optional.empty());

        assertThat(repository().countDisposalsSince(Fixtures.UNIT, LocalDate.now())).isZero();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldMapTheLastRejectionReasonOfEachItem() {
        UUID primeiro = UUID.randomUUID();
        UUID segundo = UUID.randomUUID();
        Neo4jClient.RecordFetchSpec<Map<String, Object>> fetch = mock(Neo4jClient.RecordFetchSpec.class);
        when(neo4jClient.query(anyString())).thenReturn(spec);
        when(spec.bindAll(anyMap())).thenReturn(spec);
        when(spec.fetch()).thenReturn(fetch);
        when(fetch.all()).thenReturn(List.of(
                Map.of("itemId", primeiro.toString(), "reason", "Foto ilegivel"),
                Map.of("itemId", segundo.toString(), "reason", "Material errado")));

        Map<UUID, String> motivos = repository().lastRejectionReasons(Fixtures.UNIT,
                List.of(primeiro, segundo));

        assertThat(motivos).containsEntry(primeiro, "Foto ilegivel")
                .containsEntry(segundo, "Material errado");
    }

    /** Sem reprovados, nem vale ir ao banco. */
    @Test
    void shouldNotQueryWithoutRejectedItems() {
        assertThat(repository().lastRejectionReasons(Fixtures.UNIT, List.of())).isEmpty();
        org.mockito.Mockito.verifyNoInteractions(neo4jClient);
    }

    @Test
    void shouldMapACategoryRowIncludingTheUncategorized() {
        UUID notebooks = UUID.randomUUID();
        CategoryItemCount comCategoria = DashboardRepositoryImpl.toCategoryCount(
                Map.of("categoryId", notebooks.toString(), "name", "Notebooks", "itemCount", 3L));
        CategoryItemCount semCategoria = DashboardRepositoryImpl.toCategoryCount(
                Map.of("itemCount", 1));

        assertThat(comCategoria.categoryId()).isEqualTo(notebooks);
        assertThat(comCategoria.name()).isEqualTo("Notebooks");
        assertThat(comCategoria.itemCount()).isEqualTo(3);
        assertThat(semCategoria.categoryId()).isNull();
        assertThat(semCategoria.name()).isNull();
        assertThat(semCategoria.itemCount()).isEqualTo(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnTheActiveItemsGroupedByCategory() {
        UUID notebooks = UUID.randomUUID();
        Neo4jClient.RecordFetchSpec<Map<String, Object>> fetch = mock(Neo4jClient.RecordFetchSpec.class);
        when(neo4jClient.query(anyString())).thenReturn(spec);
        when(spec.bindAll(anyMap())).thenReturn(spec);
        when(spec.fetch()).thenReturn(fetch);
        when(fetch.all()).thenReturn(List.of(
                Map.of("categoryId", notebooks.toString(), "name", "Notebooks", "itemCount", 3L)));

        assertThat(repository().countActiveItemsByCategory(Fixtures.UNIT))
                .containsExactly(new CategoryItemCount(notebooks, "Notebooks", 3));
    }
}
