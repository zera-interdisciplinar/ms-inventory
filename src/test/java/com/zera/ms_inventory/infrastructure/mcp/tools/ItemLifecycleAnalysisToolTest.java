package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.usecase.item.FindItemById;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemLifecycleAnalysisToolTest {

    @Mock
    private FindItemById findItemById;

    @Test
    void shouldComputeAgeFromAcquisitionDate() {
        UUID id = UUID.randomUUID();
        LocalDate acquiredAt = LocalDate.now().minusDays(400);
        Item item = new Item(id, new Barcode("123456"), ItemStatus.OK, Fixtures.UNIT,
                Fixtures.model(Fixtures.UNIT), LocalDateTime.now(), 2024, 7, "SN-001", acquiredAt);
        when(findItemById.execute(Fixtures.UNIT, id)).thenReturn(item);

        var report = new ItemLifecycleAnalysisTool(findItemById).analyzeItemLifecycle(Fixtures.UNIT, id);

        assertEquals(id, report.itemId);
        assertEquals(ChronoUnit.DAYS.between(acquiredAt, LocalDate.now()), report.ageInDays);
        assertEquals("OK", report.currentStatus);
    }

    @Test
    void shouldReportZeroAgeWhenAcquisitionDateIsMissing() {
        UUID id = UUID.randomUUID();
        Item item = new Item(id, new Barcode("123456"), ItemStatus.OK, Fixtures.UNIT,
                Fixtures.model(Fixtures.UNIT), LocalDateTime.now(), 2024, null, "SN-001", null);
        when(findItemById.execute(Fixtures.UNIT, id)).thenReturn(item);

        var report = new ItemLifecycleAnalysisTool(findItemById).analyzeItemLifecycle(Fixtures.UNIT, id);

        assertEquals(0, report.ageInDays);
        assertEquals(0, report.usageIntensity);
    }

    @Test
    void shouldRejectMissingUnitIdInsteadOfFallingBackToAGlobalRead() {
        ItemLifecycleAnalysisTool tool = new ItemLifecycleAnalysisTool(findItemById);
        UUID id = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> tool.analyzeItemLifecycle(null, id));
        verifyNoInteractions(findItemById);
    }
}
