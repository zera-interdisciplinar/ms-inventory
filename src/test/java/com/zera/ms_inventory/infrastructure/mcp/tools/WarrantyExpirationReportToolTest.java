package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.usecase.item.FindAllItems;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarrantyExpirationReportToolTest {

    @Mock
    private FindAllItems findAllItems;

    private Model modelWithWarranty(Integer months) {
        return new Model(UUID.randomUUID(), Fixtures.UNIT, "Laptop", "Acme", months, 60, Set.of(),
                Fixtures.category(Fixtures.UNIT));
    }

    private Item item(Model model, LocalDate acquiredAt) {
        return new Item(UUID.randomUUID(), new Barcode("123456"), ItemStatus.OK, Fixtures.UNIT, model,
                LocalDateTime.now(), 2024, 7, "SN-001", acquiredAt);
    }

    @Test
    void shouldUseTheModelWarrantyMonthsNotAFixedThreeYears() {
        // 12 meses de garantia, comprado ha 11 meses: vence em ~30 dias
        Item expiring = item(modelWithWarranty(12), LocalDate.now().minusMonths(12).plusDays(10));
        // 36 meses de garantia na mesma data de compra: nao vence agora
        Item notExpiring = item(modelWithWarranty(36), LocalDate.now().minusMonths(12).plusDays(10));
        when(findAllItems.execute(Fixtures.UNIT)).thenReturn(List.of(expiring, notExpiring));

        List<WarrantyExpirationReportTool.WarrantyExpiringItem> result =
                new WarrantyExpirationReportTool(findAllItems)
                        .getWarrantyExpirationReport(Fixtures.UNIT, 30, null, null);

        assertEquals(1, result.size());
        assertEquals(expiring.getId(), result.get(0).itemId);
        assertEquals(expiring.getAcquiredAt().plusMonths(12), result.get(0).expiryDate);
    }

    @Test
    void shouldSkipItemsWithoutAcquisitionDateModelOrWarranty() {
        when(findAllItems.execute(Fixtures.UNIT)).thenReturn(List.of(
                item(modelWithWarranty(12), null),
                item(null, LocalDate.now()),
                item(modelWithWarranty(null), LocalDate.now())));

        List<WarrantyExpirationReportTool.WarrantyExpiringItem> result =
                new WarrantyExpirationReportTool(findAllItems)
                        .getWarrantyExpirationReport(Fixtures.UNIT, null, null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldApplyLimitAndOffset() {
        LocalDate acquiredAt = LocalDate.now().minusMonths(12).plusDays(10);
        when(findAllItems.execute(Fixtures.UNIT)).thenReturn(List.of(
                item(modelWithWarranty(12), acquiredAt),
                item(modelWithWarranty(12), acquiredAt)));

        List<WarrantyExpirationReportTool.WarrantyExpiringItem> result =
                new WarrantyExpirationReportTool(findAllItems)
                        .getWarrantyExpirationReport(Fixtures.UNIT, 30, 1, 1);

        assertEquals(1, result.size());
    }

    @Test
    void shouldRejectMissingUnitIdInsteadOfFallingBackToAGlobalRead() {
        WarrantyExpirationReportTool tool = new WarrantyExpirationReportTool(findAllItems);

        assertThrows(IllegalArgumentException.class,
                () -> tool.getWarrantyExpirationReport(null, 30, null, null));
        verifyNoInteractions(findAllItems);
    }
}
