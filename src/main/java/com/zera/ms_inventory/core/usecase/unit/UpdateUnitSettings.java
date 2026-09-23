package com.zera.ms_inventory.core.usecase.unit;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.UnitInventorySettings;
import com.zera.ms_inventory.core.domain.valueobject.Actor;

public interface UpdateUnitSettings {
    UnitInventorySettings execute(UUID unitId, Integer stockCapacity, Actor actor);
}
