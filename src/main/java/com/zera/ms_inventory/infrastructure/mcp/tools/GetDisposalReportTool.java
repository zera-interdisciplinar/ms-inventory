package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.usecase.disposal.DisposalReport;
import com.zera.ms_inventory.core.usecase.disposal.GetDisposalReport;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * O relatorio de cotacao precisa do descarte e dos itens no mesmo formato do PDF. list_disposals
 * nao traz marca, modelo, serie nem condicao.
 */
@Component
public class GetDisposalReportTool {

    private final GetDisposalReport getDisposalReport;

    public GetDisposalReportTool(GetDisposalReport getDisposalReport) {
        this.getDisposalReport = getDisposalReport;
    }

    @McpTool(
        name = "get_disposal_report",
        description = "Report payload for one disposal: header (quote_number, issued_at, "
                + "proposal_deadline, requester, owner) and one row per asset (title, equipment_type, "
                + "brand, model, quantity, asset_number, serial_number, origin, status, description). "
                + "quote_number, proposal_deadline and requester are empty until those fields exist. "
                + "status is the physical condition (NEW, USED, SEMI_DAMAGED, DAMAGED). "
                + "origin is the unit id.",
        annotations = @McpTool.McpAnnotations(
            readOnlyHint = true,
            title = "Get Disposal Report"
        )
    )
    public DisposalReport getDisposalReport(
            @McpToolParam(description = McpToolScope.UNIT_ID_DESCRIPTION, required = true) UUID unitId,
            @McpToolParam(description = "UUID of the disposal", required = true) UUID disposalId) {
        return getDisposalReport.execute(McpToolScope.require(unitId), disposalId);
    }
}
