package com.zera.ms_inventory.core.domain.valueobject;

/**
 * Contagens do estoque da unidade, lidas numa consulta so. {@code activeItemsBefore} e quantos
 * itens estavam no estoque no inicio da janela, usado para a variacao do painel.
 */
public record InventoryCounts(
        long activeItems,
        long activeItemsBefore,
        long pendingApproval,
        long inMaintenance,
        long awaitingEvaluation,
        long draft,
        long rejected
) {
    public static InventoryCounts empty() {
        return new InventoryCounts(0, 0, 0, 0, 0, 0, 0);
    }

    /** Variacao percentual do estoque na janela; vazia quando nao havia nada com que comparar. */
    public java.util.OptionalDouble activeItemsChangePercent() {
        if (activeItemsBefore == 0) {
            return java.util.OptionalDouble.empty();
        }
        return java.util.OptionalDouble.of((activeItems - activeItemsBefore) * 100.0 / activeItemsBefore);
    }
}
