package com.zera.ms_inventory.core.usecase.disposal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Disposal;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.exception.DisposalNotFoundException;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.DestinationType;
import com.zera.ms_inventory.core.domain.valueobject.DisposedItem;
import com.zera.ms_inventory.core.domain.valueobject.ItemCondition;
import com.zera.ms_inventory.core.usecase.item.FindItemById;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetDisposalReportImplTest {

    @Mock private FindDisposalById findDisposalById;
    @Mock private FindItemById findItemById;

    private GetDisposalReportImpl useCase() {
        return new GetDisposalReportImpl(findDisposalById, findItemById);
    }

    @Test
    void shouldMapTheHeaderAndEachAssetFromTheItem() {
        UUID notebookId = UUID.randomUUID();
        UUID monitorId = UUID.randomUUID();
        Item notebook = described(notebookId, "tela riscada", ItemCondition.DAMAGED);
        Item monitor = described(monitorId, null, ItemCondition.USED);
        Disposal disposal = disposal(List.of(
                new DisposedItem(notebookId, "100001", "Notebook", 2.0),
                new DisposedItem(monitorId, "100002", "Monitor", 1.0)));
        when(findDisposalById.execute(Fixtures.UNIT, disposal.getId())).thenReturn(disposal);
        when(findItemById.execute(Fixtures.UNIT, notebookId)).thenReturn(notebook);
        when(findItemById.execute(Fixtures.UNIT, monitorId)).thenReturn(monitor);

        DisposalReport report = useCase().execute(Fixtures.UNIT, disposal.getId());

        assertThat(report.quoteNumber()).isEmpty();
        assertThat(report.proposalDeadline()).isEmpty();
        assertThat(report.requester()).isEmpty();
        assertThat(report.issuedAt()).isEqualTo("2026-03-15");
        assertThat(report.owner()).isEqualTo("Gustavo Operario");
        assertThat(report.items()).containsExactly(
                new DisposalReport.Line(
                        "Item 1 - Notebook", "Electronics", "Acme", "Laptop X1", "1",
                        "100001", "SN-001", Fixtures.UNIT.toString(), "DAMAGED", "tela riscada"),
                new DisposalReport.Line(
                        "Item 2 - Monitor", "Electronics", "Acme", "Laptop X1", "1",
                        "100002", "SN-001", Fixtures.UNIT.toString(), "USED", ""));
    }

    /** O snapshot continua valendo se o no do item sumiu; o PDF nao pode pular a linha. */
    @Test
    void shouldKeepTheSnapshotWhenTheItemIsGone() {
        UUID missingId = UUID.randomUUID();
        Disposal disposal = disposal(List.of(new DisposedItem(missingId, "100001", "Notebook", 2.0)));
        when(findDisposalById.execute(Fixtures.UNIT, disposal.getId())).thenReturn(disposal);
        when(findItemById.execute(Fixtures.UNIT, missingId)).thenThrow(new ItemNotFoundException(missingId));

        DisposalReport.Line line = useCase().execute(Fixtures.UNIT, disposal.getId()).items().get(0);

        assertThat(line).isEqualTo(new DisposalReport.Line(
                "Item 1 - Notebook", "", "", "", "1", "100001", "", Fixtures.UNIT.toString(), "", ""));
    }

    @Test
    void shouldLeaveItemFieldsEmptyWhenTheModelOrConditionIsMissing() {
        UUID itemId = UUID.randomUUID();
        Item item = Fixtures.item(itemId, Fixtures.UNIT, null);
        Disposal disposal = disposal(List.of(new DisposedItem(itemId, null, null, null)));
        when(findDisposalById.execute(Fixtures.UNIT, disposal.getId())).thenReturn(disposal);
        when(findItemById.execute(Fixtures.UNIT, itemId)).thenReturn(item);

        DisposalReport.Line line = useCase().execute(Fixtures.UNIT, disposal.getId()).items().get(0);

        assertThat(line.title()).isEqualTo("Item 1 - ");
        assertThat(line.equipmentType()).isEmpty();
        assertThat(line.brand()).isEmpty();
        assertThat(line.model()).isEmpty();
        assertThat(line.assetNumber()).isEmpty();
        assertThat(line.status()).isEmpty();
        assertThat(line.quantity()).isEqualTo("1");
    }

    @Test
    void shouldPropagateAMissingDisposal() {
        UUID id = UUID.randomUUID();
        when(findDisposalById.execute(Fixtures.UNIT, id)).thenThrow(new DisposalNotFoundException(id));

        assertThatThrownBy(() -> useCase().execute(Fixtures.UNIT, id))
                .isInstanceOf(DisposalNotFoundException.class);
    }

    private static Item described(UUID id, String notes, ItemCondition condition) {
        Item item = Fixtures.item(id, Fixtures.UNIT);
        item.describe("ignorado pelo titulo", condition, false, Set.of(), notes);
        return item;
    }

    private static Disposal disposal(List<DisposedItem> items) {
        LocalDateTime issuedAt = LocalDateTime.of(2026, 3, 15, 10, 30);
        return new Disposal(UUID.randomUUID(), Fixtures.UNIT, DestinationType.RECYCLING, null, null,
                null, null, items, Fixtures.OPERATOR.userId(), Fixtures.OPERATOR.name(),
                issuedAt, issuedAt);
    }
}
