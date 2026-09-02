package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.usecase.item.FindAllItems;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarrantyExpirationReportToolTest {

    @Mock
    private FindAllItems findAllItems;

    @Test
    void shouldExcludeItemsWithoutAcquiredAt() {
        Item noAcquiredAt = new Item(UUID.randomUUID(), new Barcode("BC-1"), ItemStatus.OK, null,
            null, null, null, null, "SN-1", null);
        when(findAllItems.execute()).thenReturn(List.of(noAcquiredAt));

        WarrantyExpirationReportTool tool = new WarrantyExpirationReportTool(findAllItems);

        assertEquals(0, tool.getWarrantyExpirationReport(30, null, null).size());
    }

    @Test
    void shouldIncludeItemsExpiringWithinWindow() {
        LocalDate acquiredAt = LocalDate.now().minusYears(3).plusDays(10);
        Item expiringSoon = new Item(UUID.randomUUID(), new Barcode("BC-2"), ItemStatus.OK, null,
            null, null, null, null, "SN-2", acquiredAt);
        when(findAllItems.execute()).thenReturn(List.of(expiringSoon));

        WarrantyExpirationReportTool tool = new WarrantyExpirationReportTool(findAllItems);

        List<WarrantyExpirationReportTool.WarrantyExpiringItem> result = tool.getWarrantyExpirationReport(30, null, null);

        assertEquals(1, result.size());
        assertEquals(acquiredAt.plusYears(3), result.get(0).estimatedExpiryDate);
    }

    @Test
    void shouldExcludeItemsFarFromExpiring() {
        LocalDate acquiredAt = LocalDate.now();
        Item freshItem = new Item(UUID.randomUUID(), new Barcode("BC-3"), ItemStatus.OK, null,
            null, null, null, null, "SN-3", acquiredAt);
        when(findAllItems.execute()).thenReturn(List.of(freshItem));

        WarrantyExpirationReportTool tool = new WarrantyExpirationReportTool(findAllItems);

        assertEquals(0, tool.getWarrantyExpirationReport(30, null, null).size());
    }
}
