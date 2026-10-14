package com.zera.ms_inventory.core.usecase.disposal;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Forma que o PDF de cotacao ja espera. Numero de cotacao, prazo e solicitante ainda nao existem
 * no descarte: saem vazios para o contrato nao mudar quando forem preenchidos.
 */
public record DisposalReport(
        @JsonProperty("quote_number") String quoteNumber,
        @JsonProperty("issued_at") String issuedAt,
        @JsonProperty("proposal_deadline") String proposalDeadline,
        String requester,
        String owner,
        List<Line> items
) {
    public record Line(
            String title,
            @JsonProperty("equipment_type") String equipmentType,
            String brand,
            String model,
            String quantity,
            @JsonProperty("asset_number") String assetNumber,
            @JsonProperty("serial_number") String serialNumber,
            String origin,
            String status,
            String description
    ) {
    }
}
