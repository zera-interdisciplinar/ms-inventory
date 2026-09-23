package com.zera.ms_inventory.core.usecase.dashboard;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.entity.UnitInventorySettings;
import com.zera.ms_inventory.core.domain.valueobject.HomeSummary;
import com.zera.ms_inventory.core.domain.valueobject.InventoryCounts;
import com.zera.ms_inventory.core.domain.valueobject.ItemCondition;
import com.zera.ms_inventory.core.domain.valueobject.ItemFilter;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
import com.zera.ms_inventory.core.domain.valueobject.WorkCenterSummary;
import com.zera.ms_inventory.core.repository.DashboardRepository;
import com.zera.ms_inventory.core.repository.ItemRepository;
import com.zera.ms_inventory.core.usecase.unit.GetUnitSettings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardUseCasesTest {

    @Mock private DashboardRepository dashboardRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private GetUnitSettings getUnitSettings;

    private DashboardUseCases useCase() {
        return new DashboardUseCases(dashboardRepository, itemRepository, getUnitSettings);
    }

    private InventoryCounts counts(long ativos, long antes) {
        return new InventoryCounts(ativos, antes, 3, 2, 1, 4, 5);
    }

    private UnitInventorySettings settings(Integer capacidade) {
        UnitInventorySettings settings = UnitInventorySettings.notConfigured(Fixtures.UNIT);
        if (capacidade != null) {
            settings.changeCapacity(capacidade, Fixtures.MANAGER);
        }
        return settings;
    }

    // ---- painel inicial ----

    @Test
    void shouldSummarizeTheHomePanel() {
        when(dashboardRepository.countsOf(any(), any())).thenReturn(counts(120, 100));
        when(dashboardRepository.countDisposalsSince(any(), any())).thenReturn(7L);
        when(getUnitSettings.execute(Fixtures.UNIT)).thenReturn(settings(200));
        when(itemRepository.findPage(any(), any(), any()))
                .thenReturn(new PageResult<>(List.of(Fixtures.item(Fixtures.UNIT)), 0, 5, 1));

        HomeSummary resumo = useCase().execute(Fixtures.UNIT);

        assertThat(resumo.activeItems()).isEqualTo(120);
        // 120 contra 100 na janela
        assertThat(resumo.activeItemsChangePercent()).isEqualTo(20.0);
        assertThat(resumo.stockCapacity()).isEqualTo(200);
        assertThat(resumo.occupancyPercent()).isEqualTo(60.0);
        assertThat(resumo.pendingApproval()).isEqualTo(3);
        assertThat(resumo.inMaintenance()).isEqualTo(2);
        assertThat(resumo.awaitingEvaluation()).isEqualTo(1);
        assertThat(resumo.disposalsInWindow()).isEqualTo(7);
        assertThat(resumo.windowDays()).isEqualTo(30);
        assertThat(resumo.recentItems()).hasSize(1);
    }

    /** Sem capacidade configurada a ocupacao nao existe; nao pode virar 0% nem erro. */
    @Test
    void shouldLeaveOccupancyNullWithoutCapacity() {
        when(dashboardRepository.countsOf(any(), any())).thenReturn(counts(50, 40));
        when(dashboardRepository.countDisposalsSince(any(), any())).thenReturn(0L);
        when(getUnitSettings.execute(Fixtures.UNIT)).thenReturn(settings(null));
        when(itemRepository.findPage(any(), any(), any()))
                .thenReturn(new PageResult<>(List.of(), 0, 5, 0));

        HomeSummary resumo = useCase().execute(Fixtures.UNIT);

        assertThat(resumo.occupancyPercent()).isNull();
        assertThat(resumo.stockCapacity()).isNull();
    }

    /** Unidade que comecou vazia nao tem base de comparacao. */
    @Test
    void shouldLeaveTheChangeNullWithoutPreviousStock() {
        when(dashboardRepository.countsOf(any(), any())).thenReturn(counts(10, 0));
        when(dashboardRepository.countDisposalsSince(any(), any())).thenReturn(0L);
        when(getUnitSettings.execute(Fixtures.UNIT)).thenReturn(settings(100));
        when(itemRepository.findPage(any(), any(), any()))
                .thenReturn(new PageResult<>(List.of(), 0, 5, 0));

        assertThat(useCase().execute(Fixtures.UNIT).activeItemsChangePercent()).isNull();
    }

    @Test
    void shouldAskForTheFiveMostRecentItems() {
        when(dashboardRepository.countsOf(any(), any())).thenReturn(counts(1, 1));
        when(dashboardRepository.countDisposalsSince(any(), any())).thenReturn(0L);
        when(getUnitSettings.execute(Fixtures.UNIT)).thenReturn(settings(10));
        when(itemRepository.findPage(any(), any(), any()))
                .thenReturn(new PageResult<>(List.of(), 0, 5, 0));

        useCase().execute(Fixtures.UNIT);

        verify(itemRepository).findPage(Fixtures.UNIT, ItemFilter.none(), new Pagination(0, 5));
    }

    // ---- central de trabalho ----

    private Item itemAt(ItemStatus status) {
        Item item = Fixtures.item(UUID.randomUUID(), Fixtures.UNIT);
        item.restoreStatus(status);
        return item;
    }

    @Test
    void shouldSeparateTheUserPendenciesFromTheUnitOnes() {
        Item rascunho = itemAt(ItemStatus.DRAFT);
        Item reprovado = itemAt(ItemStatus.REJECTED);
        Item danificado = itemAt(ItemStatus.IN_STOCK);
        danificado.describe("Notebook", ItemCondition.DAMAGED, false, Set.of(), null);
        lenient().when(dashboardRepository.countsOf(any(), any())).thenReturn(counts(9, 9));
        when(itemRepository.findPage(any(), any(), any()))
                .thenReturn(new PageResult<>(List.of(rascunho), 0, 20, 1))
                .thenReturn(new PageResult<>(List.of(reprovado), 0, 20, 1))
                .thenReturn(new PageResult<>(List.of(danificado), 0, 20, 1));
        when(dashboardRepository.lastRejectionReasons(Fixtures.UNIT, List.of(reprovado.getId())))
                .thenReturn(Map.of(reprovado.getId(), "Foto ilegivel"));

        WorkCenterSummary central = useCase().execute(Fixtures.UNIT, Fixtures.OPERATOR);

        assertThat(central.drafts()).containsExactly(rascunho);
        assertThat(central.rejected()).containsExactly(reprovado);
        assertThat(central.rejectionReasons()).containsEntry(reprovado.getId(), "Foto ilegivel");
        assertThat(central.damagedWithoutDestination()).containsExactly(danificado);
        assertThat(central.inMaintenance()).isEqualTo(2);
        assertThat(central.awaitingEvaluation()).isEqualTo(1);

        ArgumentCaptor<ItemFilter> filtros = ArgumentCaptor.forClass(ItemFilter.class);
        verify(itemRepository, org.mockito.Mockito.times(3))
                .findPage(any(), filtros.capture(), any());
        // rascunho e reprovado sao do usuario logado
        assertThat(filtros.getAllValues().get(0).createdBy()).isEqualTo(Fixtures.OPERATOR.userId());
        assertThat(filtros.getAllValues().get(0).status()).isEqualTo(ItemStatus.DRAFT);
        assertThat(filtros.getAllValues().get(1).createdBy()).isEqualTo(Fixtures.OPERATOR.userId());
        assertThat(filtros.getAllValues().get(1).status()).isEqualTo(ItemStatus.REJECTED);
        // danificado sem destino e da unidade toda, e usa a elegibilidade para descarte
        assertThat(filtros.getAllValues().get(2).createdBy()).isNull();
        assertThat(filtros.getAllValues().get(2).condition()).isEqualTo(ItemCondition.DAMAGED);
        assertThat(filtros.getAllValues().get(2).onlyEligibleForDisposal()).isTrue();
    }

    @Test
    void shouldNotLookForReasonsWithoutRejectedItems() {
        lenient().when(dashboardRepository.countsOf(any(), any())).thenReturn(counts(0, 0));
        when(itemRepository.findPage(any(), any(), any()))
                .thenReturn(new PageResult<>(List.of(), 0, 20, 0));
        when(dashboardRepository.lastRejectionReasons(Fixtures.UNIT, List.of())).thenReturn(Map.of());

        WorkCenterSummary central = useCase().execute(Fixtures.UNIT, Fixtures.OPERATOR);

        assertThat(central.rejectionReasons()).isEmpty();
        assertThat(central.drafts()).isEmpty();
    }

    @Test
    void shouldUseTheSameThirtyDayWindowForCountsAndDisposals() {
        when(dashboardRepository.countsOf(any(), any())).thenReturn(counts(1, 1));
        when(dashboardRepository.countDisposalsSince(any(), any())).thenReturn(0L);
        when(getUnitSettings.execute(Fixtures.UNIT)).thenReturn(settings(10));
        when(itemRepository.findPage(any(), any(), any()))
                .thenReturn(new PageResult<>(List.of(), 0, 5, 0));

        useCase().execute(Fixtures.UNIT);

        ArgumentCaptor<LocalDateTime> desde = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(dashboardRepository).countsOf(org.mockito.ArgumentMatchers.eq(Fixtures.UNIT), desde.capture());
        assertThat(desde.getValue().toLocalDate()).isEqualTo(LocalDate.now().minusDays(30));
        verify(dashboardRepository).countDisposalsSince(Fixtures.UNIT, LocalDate.now().minusDays(30));
    }
}
