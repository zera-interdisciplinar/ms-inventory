package com.zera.ms_inventory.core.usecase.unit;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.UnitInventorySettings;

public interface GetUnitSettings {
    UnitInventorySettings execute(UUID unitId);
}
