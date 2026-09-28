package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.core.domain.valueobject.ItemCondition;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.usecase.item.FindAllItems;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryHealthToolTest {

    @Mock
    private FindAllItems findAllItems;

    private Item item(ItemStatus status, String serialNumber) {
        return new Item(UUID.randomUUID(), new Barcode("123456"), status, Fixtures.UNIT,
                Fixtures.model(Fixtures.UNIT), null, 2024, 6, serialNumber, LocalDate.now());
    }

    private static Item condition(Item item, ItemCondition condition) {
        item.describe(item.getName(), condition, false, Set.of(), null);
        return item;
    }

    private static Item predictedFor(Item item, LocalDate date) {
        item.recordPrediction(date);
        return item;
    }

    @Test
    void shouldReturnZeroedReportWhenTheUnitHasNoItems() {
        when(findAllItems.execute(Fixtures.UNIT)).thenReturn(List.of());

        var report = new InventoryHealthTool(findAllItems).getInventoryHealth(Fixtures.UNIT);

        assertEquals(0, report.totalItems());
        assertEquals(0.0, report.healthScore());
        assertEquals(0, report.itemsByStatus().size());
    }

    /** O relatorio antigo so sabia somar "ok" e "danificado"; o v1 tem oito estados. */
    @Test
    void shouldBreakTheUnitDownByStatusAndCondition() {
        when(findAllItems.execute(Fixtures.UNIT)).thenReturn(List.of(
                item(ItemStatus.IN_STOCK, "SN-001"),
                item(ItemStatus.IN_STOCK, "SN-002"),
                item(ItemStatus.PENDING_APPROVAL, "SN-003"),
                condition(item(ItemStatus.IN_MAINTENANCE, "SN-004"), ItemCondition.DAMAGED)));

        var report = new InventoryHealthTool(findAllItems).getInventoryHealth(Fixtures.UNIT);

        assertEquals(4, report.totalItems());
        assertEquals(2, report.itemsByStatus().get("IN_STOCK"));
        assertEquals(1, report.itemsByStatus().get("PENDING_APPROVAL"));
        assertEquals(1, report.itemsByStatus().get("IN_MAINTENANCE"));
        assertEquals(1, report.itemsByCondition().get("DAMAGED"));
        assertEquals(1, report.itemsAwaitingApproval());
        assertEquals(1, report.itemsInMaintenance());
    }

    /** Estado que ninguem usa nao vira linha de zero no relatorio, que so encheria o contexto. */
    @Test
    void shouldLeaveOutStatusesWithNoItems() {
        when(findAllItems.execute(Fixtures.UNIT)).thenReturn(List.of(item(ItemStatus.IN_STOCK, "SN-001")));

        var report = new InventoryHealthTool(findAllItems).getInventoryHealth(Fixtures.UNIT);

        assertEquals(1, report.itemsByStatus().size());
        assertFalse(report.itemsByStatus().containsKey("DISPOSED"));
    }

    /**
     * Item descartado nao e "saudavel": o score antigo contava qualquer item nao danificado,
     * entao uma unidade que descartou tudo aparecia com 100%.
     */
    @Test
    void shouldScoreOnlyWhatIsStillUsable() {
        when(findAllItems.execute(Fixtures.UNIT)).thenReturn(List.of(
                item(ItemStatus.IN_STOCK, "SN-001"),
                item(ItemStatus.DISPOSED, "SN-002"),
                condition(item(ItemStatus.IN_STOCK, "SN-003"), ItemCondition.DAMAGED),
                item(ItemStatus.IN_STOCK, "SN-004")));

        var report = new InventoryHealthTool(findAllItems).getInventoryHealth(Fixtures.UNIT);

        assertEquals(50.0, report.healthScore(), 0.0001);
    }

    @Test
    void shouldCountItemsMissingDataAndItemsFailingSoon() {
        when(findAllItems.execute(Fixtures.UNIT)).thenReturn(List.of(
                predictedFor(item(ItemStatus.IN_STOCK, "SN-001"), LocalDate.now().plusDays(10)),
                predictedFor(item(ItemStatus.IN_STOCK, "SN-002"), LocalDate.now().plusYears(3)),
                item(ItemStatus.IN_STOCK, null)));

        var report = new InventoryHealthTool(findAllItems).getInventoryHealth(Fixtures.UNIT);

        assertEquals(1, report.itemsWithoutSerialNumber());
        assertEquals(1, report.itemsWithoutPrediction());
        assertEquals(1, report.itemsPredictedToFailWithin90Days());
    }

    @Test
    void shouldRejectMissingUnitIdInsteadOfFallingBackToAGlobalRead() {
        InventoryHealthTool tool = new InventoryHealthTool(findAllItems);

        assertThrows(IllegalArgumentException.class, () -> tool.getInventoryHealth(null));
        verifyNoInteractions(findAllItems);
    }
}
