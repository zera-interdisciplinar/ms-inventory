package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.usecase.disposal.DisposalReport;
import com.zera.ms_inventory.core.usecase.disposal.GetDisposalReport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetDisposalReportToolTest {

    @Mock private GetDisposalReport getDisposalReport;

    @Test
    void shouldReturnTheReportForTheUnit() {
        UUID disposalId = UUID.randomUUID();
        DisposalReport report = new DisposalReport("", "2026-03-15", "", "", "Gustavo Operario", List.of());
        when(getDisposalReport.execute(Fixtures.UNIT, disposalId)).thenReturn(report);

        assertThat(new GetDisposalReportTool(getDisposalReport).getDisposalReport(Fixtures.UNIT, disposalId))
                .isSameAs(report);
    }

    @Test
    void shouldRejectMissingUnitIdInsteadOfFallingBackToAGlobalRead() {
        GetDisposalReportTool tool = new GetDisposalReportTool(getDisposalReport);

        assertThrows(IllegalArgumentException.class, () -> tool.getDisposalReport(null, UUID.randomUUID()));
        verifyNoInteractions(getDisposalReport);
    }
}
