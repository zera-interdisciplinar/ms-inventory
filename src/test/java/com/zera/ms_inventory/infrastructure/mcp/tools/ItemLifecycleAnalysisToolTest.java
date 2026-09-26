package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
import com.zera.ms_inventory.core.usecase.item.FindItemById;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemLifecycleAnalysisToolTest {

    @Mock
    private FindItemById findItemById;

    private Item stored(UUID id, Model model, LocalDate acquiredAt, Integer usageIntensity) {
        Item item = new Item(id, new Barcode("123456"), ItemStatus.IN_STOCK, Fixtures.UNIT, model,
                null, 2024, usageIntensity, "SN-001", acquiredAt);
        when(findItemById.execute(Fixtures.UNIT, id)).thenReturn(item);
        return item;
    }

    private static Model model(Integer warrantyMonths, Integer lifespanMonths) {
        return new Model(UUID.randomUUID(), Fixtures.UNIT, "Laptop", "Acme", warrantyMonths, lifespanMonths,
                Set.of(), null, null, Fixtures.category(Fixtures.UNIT));
    }

    @Test
    void shouldComputeAgeFromAcquisitionDate() {
        UUID id = UUID.randomUUID();
        LocalDate acquiredAt = LocalDate.now().minusDays(400);
        stored(id, model(24, 60), acquiredAt, 6);

        var report = new ItemLifecycleAnalysisTool(findItemById).analyzeItemLifecycle(Fixtures.UNIT, id);

        assertEquals(id, report.item().itemId());
        assertEquals(ChronoUnit.DAYS.between(acquiredAt, LocalDate.now()), report.ageInDays());
        assertEquals("IN_STOCK", report.item().status());
    }

    /** A descricao antiga anunciava LOW/MEDIUM/HIGH; o valor sempre foi o inteiro de 0 a 10. */
    @Test
    void shouldReportUsageIntensityAsTheIntegerScale() {
        UUID id = UUID.randomUUID();
        stored(id, model(24, 60), LocalDate.now().minusDays(10), 8);

        var report = new ItemLifecycleAnalysisTool(findItemById).analyzeItemLifecycle(Fixtures.UNIT, id);

        assertEquals(8, report.item().usageIntensity());
    }

    /** Subtrair datas e onde o assistente erra, entao os dias restantes vao prontos. */
    @Test
    void shouldComputeWarrantyAndEndOfLifeWithTheDaysRemaining() {
        UUID id = UUID.randomUUID();
        LocalDate acquiredAt = LocalDate.now().minusMonths(12);
        stored(id, model(24, 60), acquiredAt, 5);

        var report = new ItemLifecycleAnalysisTool(findItemById).analyzeItemLifecycle(Fixtures.UNIT, id);

        assertEquals(acquiredAt.plusMonths(24), report.warrantyExpiresOn());
        assertEquals(acquiredAt.plusMonths(60), report.expectedEndOfLifeOn());
        assertTrue(report.daysUntilWarrantyExpires() > 0);
    }

    /** Garantia vencida precisa aparecer como negativo, nao como ausencia de informacao. */
    @Test
    void shouldReportExpiredWarrantyAsNegativeDays() {
        UUID id = UUID.randomUUID();
        stored(id, model(12, 60), LocalDate.now().minusMonths(24), 5);

        var report = new ItemLifecycleAnalysisTool(findItemById).analyzeItemLifecycle(Fixtures.UNIT, id);

        assertTrue(report.daysUntilWarrantyExpires() < 0);
    }

    @Test
    void shouldLeaveDatesEmptyWhenTheItemOrModelHasNoBasis() {
        UUID id = UUID.randomUUID();
        stored(id, model(null, null), null, null);

        var report = new ItemLifecycleAnalysisTool(findItemById).analyzeItemLifecycle(Fixtures.UNIT, id);

        assertNull(report.ageInDays());
        assertNull(report.warrantyExpiresOn());
        assertNull(report.expectedEndOfLifeOn());
        assertNull(report.daysUntilPredictedFailure());
        assertEquals(2024, report.item().manufacturingYear());
    }

    @Test
    void shouldReportTheDaysLeftUntilThePredictedFailure() {
        UUID id = UUID.randomUUID();
        Item item = stored(id, model(24, 60), LocalDate.now().minusDays(30), 5);
        item.recordPrediction(LocalDate.now().plusDays(45));

        var report = new ItemLifecycleAnalysisTool(findItemById).analyzeItemLifecycle(Fixtures.UNIT, id);

        assertEquals(45, report.daysUntilPredictedFailure());
    }

    @Test
    void shouldRejectMissingUnitIdInsteadOfFallingBackToAGlobalRead() {
        ItemLifecycleAnalysisTool tool = new ItemLifecycleAnalysisTool(findItemById);
        UUID id = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> tool.analyzeItemLifecycle(null, id));
        verifyNoInteractions(findItemById);
    }
}
