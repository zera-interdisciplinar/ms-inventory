package com.zera.ms_inventory.infrastructure.http.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.UnitInventorySettings;

/** {@code stockCapacity} nulo significa unidade que ainda nao configurou o estoque. */
public record UnitSettingsResponse(
        UUID unitId,
        Integer stockCapacity,
        boolean configured,
        UUID updatedBy,
        String updatedByName,
        LocalDateTime updatedAt
) {
    public static UnitSettingsResponse from(UnitInventorySettings settings) {
        if (settings == null) {
            return null;
        }
        return new UnitSettingsResponse(settings.getUnitId(), settings.getStockCapacity(),
                settings.isConfigured(), settings.getUpdatedBy(), settings.getUpdatedByName(),
                settings.getUpdatedAt());
    }
}
