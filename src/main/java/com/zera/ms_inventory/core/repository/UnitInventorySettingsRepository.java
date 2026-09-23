package com.zera.ms_inventory.core.repository;

import java.util.Optional;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.UnitInventorySettings;

public interface UnitInventorySettingsRepository {
    Optional<UnitInventorySettings> findByUnit(UUID unitId);
    UnitInventorySettings save(UnitInventorySettings settings);
}
