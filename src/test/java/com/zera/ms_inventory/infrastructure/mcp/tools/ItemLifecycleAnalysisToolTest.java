package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.usecase.item.FindItemById;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemLifecycleAnalysisToolTest {

    @Mock
    private FindItemById findItemById;

    @Test
    void shouldComputeAgeWhenAcquiredAtPresent() {
        UUID itemId = UUID.randomUUID();
        LocalDate acquiredAt = LocalDate.now().minusDays(10);
        Item item = new Item(itemId, new Barcode("BC-1"), ItemStatus.OK, UUID.randomUUID(),
            null, null, 202001, 5, "SN-1", acquiredAt);
        when(findItemById.execute(itemId)).thenReturn(item);

        ItemLifecycleAnalysisTool tool = new ItemLifecycleAnalysisTool(findItemById);

        ItemLifecycleAnalysisTool.ItemLifecycleReport report = tool.analyzeItemLifecycle(itemId);

        assertEquals(10, report.ageInDays);
        assertEquals(5, report.usageIntensity);
        assertEquals("OK", report.currentStatus);
    }

    @Test
    void shouldDefaultAgeWhenAcquiredAtMissing() {
        UUID itemId = UUID.randomUUID();
        Item item = new Item(itemId, new Barcode("BC-2"), ItemStatus.DAMAGED, null,
            null, null, null, null, null, null);
        when(findItemById.execute(itemId)).thenReturn(item);

        ItemLifecycleAnalysisTool tool = new ItemLifecycleAnalysisTool(findItemById);

        ItemLifecycleAnalysisTool.ItemLifecycleReport report = tool.analyzeItemLifecycle(itemId);

        assertEquals(0, report.ageInDays);
        assertEquals(0, report.usageIntensity);
    }
}
