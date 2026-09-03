package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.util.UUID;

final class McpToolScope {

    static final String UNIT_ID_DESCRIPTION =
            "UUID da unidade. Preenchido automaticamente pelo sistema; nao invente nem peca ao usuario.";

    private McpToolScope() {
    }

    static UUID require(UUID unitId) {
        if (unitId == null) {
            throw new IllegalArgumentException("unitId is required");
        }
        return unitId;
    }
}
