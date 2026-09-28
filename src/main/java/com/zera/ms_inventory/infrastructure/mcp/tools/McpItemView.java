package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Item;

/**
 * O item como o assistente precisa ver. As ferramentas devolviam a entidade de dominio inteira, o
 * que enchia a janela de contexto com campos que o modelo nao usa e exportava detalhe interno num
 * contrato que nao e nosso para quebrar. Aqui entram os campos do modelo v1: codigo curto, nome,
 * estado da maquina de estados, condicao, danos e previsao de quebra.
 */
public record McpItemView(
        UUID itemId,
        String displayCode,
        String name,
        String status,
        String condition,
        List<String> damages,
        String serialNumber,
        String modelName,
        String manufacturer,
        String categoryName,
        Integer manufacturingYear,
        /** Intensidade de uso na escala 0 a 10. */
        Integer usageIntensity,
        LocalDate acquiredAt,
        LocalDate predictedFailureDate,
        LocalDateTime predictionUpdatedAt
) {
    static McpItemView of(Item item) {
        var model = item.getModel();
        return new McpItemView(
                item.getId(),
                item.getDisplayCode(),
                item.getName(),
                item.getStatus() == null ? null : item.getStatus().name(),
                item.getCondition() == null ? null : item.getCondition().name(),
                item.getDamages() == null ? List.of()
                        : item.getDamages().stream().map(Enum::name).sorted().toList(),
                item.getSerialNumber(),
                model == null ? null : model.getName(),
                model == null ? null : model.getManufacturer(),
                model == null || model.getCategory() == null ? null : model.getCategory().getName(),
                item.getManufacturingYear(),
                item.getUsageIntensity(),
                item.getAcquiredAt(),
                item.getPredictedFailureDate(),
                item.getPredictionUpdatedAt());
    }
}
