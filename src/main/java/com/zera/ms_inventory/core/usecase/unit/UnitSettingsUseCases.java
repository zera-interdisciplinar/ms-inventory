package com.zera.ms_inventory.core.usecase.unit;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zera.ms_inventory.core.domain.entity.UnitInventorySettings;
import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.repository.UnitInventorySettingsRepository;

/**
 * Configuracao de estoque da unidade. Unidade que nunca configurou nao e erro: responde a
 * configuracao vazia, e a ocupacao simplesmente nao fica disponivel ate alguem definir a capacidade.
 */
@Service
public class UnitSettingsUseCases implements GetUnitSettings, UpdateUnitSettings {

    private final UnitInventorySettingsRepository repository;

    public UnitSettingsUseCases(UnitInventorySettingsRepository repository) {
        this.repository = repository;
    }

    @Override
    public UnitInventorySettings execute(UUID unitId) {
        return repository.findByUnit(unitId)
                .orElseGet(() -> UnitInventorySettings.notConfigured(unitId));
    }

    @Override
    @Transactional
    public UnitInventorySettings execute(UUID unitId, Integer stockCapacity, Actor actor) {
        UnitInventorySettings settings = repository.findByUnit(unitId)
                .orElseGet(() -> UnitInventorySettings.notConfigured(unitId));
        settings.changeCapacity(stockCapacity, actor);
        return repository.save(settings);
    }
}
