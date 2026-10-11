package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Disposal;
import com.zera.ms_inventory.core.domain.valueobject.DestinationType;
import com.zera.ms_inventory.core.domain.valueobject.DisposedItem;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
import com.zera.ms_inventory.core.usecase.disposal.ListDisposals;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListDisposalsToolTest {

    @Mock
    private ListDisposals listDisposals;

    private static Disposal disposal(DestinationType destination, DisposedItem... items) {
        return Disposal.register(Fixtures.UNIT, destination, null, "Aterro Municipal",
                LocalDate.of(2026, 9, 20), null, List.of(items), Fixtures.MANAGER);
    }

    private static DisposedItem disposed(String code, Double weightKg) {
        return new DisposedItem(UUID.randomUUID(), code, "Notebook", weightKg);
    }

    private void returning(Disposal... disposals) {
        when(listDisposals.execute(any(), any(), any()))
                .thenReturn(new PageResult<>(List.of(disposals), 0, 20, disposals.length));
    }

    @Test
    void shouldSummariseEachDisposalWithItsItemsAndTotalWeight() {
        returning(disposal(DestinationType.RECYCLING, disposed("800001", 2.5), disposed("800002", 1.5)));

        var page = new ListDisposalsTool(listDisposals).listDisposals(Fixtures.UNIT, null, null);

        assertThat(page.totalDisposals()).isEqualTo(1);
        var summary = page.disposals().get(0);
        assertThat(summary.destination()).isEqualTo("RECYCLING");
        assertThat(summary.placeName()).isEqualTo("Aterro Municipal");
        assertThat(summary.itemCount()).isEqualTo(2);
        assertThat(summary.totalWeightKg()).isEqualTo(4.0);
        assertThat(summary.items()).extracting(ListDisposalsTool.DisposedItemView::displayCode)
                .containsExactly("800001", "800002");
    }

    /**
     * Item cujo modelo nao tem peso estimado conta na lista mas nao no total; somar zero faria o
     * peso descartado parecer menor do que foi.
     */
    @Test
    void shouldReportItemsWithoutWeightInsteadOfCountingThemAsZero() {
        returning(disposal(DestinationType.LANDFILL, disposed("800001", 3.0), disposed("800002", null)));

        var summary = new ListDisposalsTool(listDisposals)
                .listDisposals(Fixtures.UNIT, null, null).disposals().get(0);

        assertThat(summary.itemCount()).isEqualTo(2);
        assertThat(summary.totalWeightKg()).isEqualTo(3.0);
        assertThat(summary.itemsWithoutWeight()).isEqualTo(1);
    }

    @Test
    void shouldCapThePageSizeSoOneCallCannotDrainTheUnit() {
        returning();

        new ListDisposalsTool(listDisposals).listDisposals(Fixtures.UNIT, 5000, -2);

        ArgumentCaptor<Pagination> pagination = ArgumentCaptor.forClass(Pagination.class);
        verify(listDisposals).execute(any(), any(), pagination.capture());
        assertThat(pagination.getValue().size()).isEqualTo(Pagination.MAX_SIZE);
        assertThat(pagination.getValue().page()).isZero();
    }

    @Test
    void shouldRejectMissingUnitIdInsteadOfFallingBackToAGlobalRead() {
        ListDisposalsTool tool = new ListDisposalsTool(listDisposals);

        assertThrows(IllegalArgumentException.class, () -> tool.listDisposals(null, null, null));
        verifyNoInteractions(listDisposals);
    }
}
