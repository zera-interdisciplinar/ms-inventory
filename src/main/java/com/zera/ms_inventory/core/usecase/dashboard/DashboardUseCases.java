package com.zera.ms_inventory.core.usecase.dashboard;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.entity.UnitInventorySettings;
import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.HomeSummary;
import com.zera.ms_inventory.core.domain.valueobject.InventoryCounts;
import com.zera.ms_inventory.core.domain.valueobject.ItemFilter;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
import com.zera.ms_inventory.core.domain.valueobject.WorkCenterSummary;
import com.zera.ms_inventory.core.repository.DashboardRepository;
import com.zera.ms_inventory.core.repository.ItemRepository;
import com.zera.ms_inventory.core.usecase.unit.GetUnitSettings;

@Service
public class DashboardUseCases implements GetHomeSummary, GetWorkCenter {

    /** Janela das variacoes e da contagem de descartes do painel. */
    static final int WINDOW_DAYS = 30;
    private static final int RECENT_ITEMS = 5;
    private static final int WORK_CENTER_LIMIT = 20;

    private final DashboardRepository dashboardRepository;
    private final ItemRepository itemRepository;
    private final GetUnitSettings getUnitSettings;

    public DashboardUseCases(DashboardRepository dashboardRepository, ItemRepository itemRepository,
                             GetUnitSettings getUnitSettings) {
        this.dashboardRepository = dashboardRepository;
        this.itemRepository = itemRepository;
        this.getUnitSettings = getUnitSettings;
    }

    @Override
    public HomeSummary execute(UUID unitId) {
        LocalDateTime since = LocalDateTime.now().minusDays(WINDOW_DAYS);
        InventoryCounts counts = dashboardRepository.countsOf(unitId, since);
        UnitInventorySettings settings = getUnitSettings.execute(unitId);

        List<Item> recent = itemRepository
                .findPage(unitId, ItemFilter.none(), new Pagination(0, RECENT_ITEMS))
                .content();

        return new HomeSummary(
                counts.activeItems(),
                counts.activeItemsChangePercent().isPresent()
                        ? counts.activeItemsChangePercent().getAsDouble() : null,
                settings.getStockCapacity(),
                settings.occupancyPercent(counts.activeItems()).isPresent()
                        ? settings.occupancyPercent(counts.activeItems()).getAsDouble() : null,
                counts.pendingApproval(),
                counts.inMaintenance(),
                counts.awaitingEvaluation(),
                dashboardRepository.countDisposalsSince(unitId, LocalDate.now().minusDays(WINDOW_DAYS)),
                WINDOW_DAYS,
                recent);
    }

    /**
     * Rascunhos e reprovados sao de quem esta logado, porque sao dele para corrigir. Os danificados
     * sem destino sao da unidade: qualquer um registra o descarte.
     */
    @Override
    public WorkCenterSummary execute(UUID unitId, Actor actor) {
        UUID author = actor != null ? actor.userId() : null;
        Pagination limite = new Pagination(0, WORK_CENTER_LIMIT);

        List<Item> drafts = itemRepository
                .findPage(unitId, ItemFilter.ownedBy(author, ItemStatus.DRAFT), limite).content();
        List<Item> rejected = itemRepository
                .findPage(unitId, ItemFilter.ownedBy(author, ItemStatus.REJECTED), limite).content();
        List<Item> damaged = itemRepository
                .findPage(unitId, ItemFilter.damagedWithoutDestination(), limite).content();

        InventoryCounts counts = dashboardRepository.countsOf(unitId,
                LocalDateTime.now().minusDays(WINDOW_DAYS));

        return new WorkCenterSummary(drafts, rejected,
                dashboardRepository.lastRejectionReasons(unitId, rejected.stream().map(Item::getId).toList()),
                damaged, counts.inMaintenance(), counts.awaitingEvaluation());
    }
}
