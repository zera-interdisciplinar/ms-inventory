package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.util.UUID;

final class McpToolScope {

    static final String UNIT_ID_DESCRIPTION =
            "UUID da unidade. Preenchido automaticamente pelo sistema; nao invente nem peca ao usuario.";

    /** Os estados do item no modelo v1; itens removidos nao aparecem em nenhuma ferramenta. */
    static final String STATUS_VALUES =
            "DRAFT, PENDING_APPROVAL, REJECTED, IN_STOCK, IN_MAINTENANCE, AWAITING_EVALUATION ou DISPOSED";

    /** Condicao fisica, independente do estado; um item IN_STOCK pode estar SEMI_DAMAGED. */
    static final String CONDITION_VALUES = "NEW, USED, SEMI_DAMAGED ou DAMAGED";

    private McpToolScope() {
    }

    static UUID require(UUID unitId) {
        if (unitId == null) {
            throw new IllegalArgumentException("unitId is required");
        }
        return unitId;
    }
}
