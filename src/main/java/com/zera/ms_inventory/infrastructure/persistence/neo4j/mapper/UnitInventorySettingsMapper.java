package com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper;

import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.UnitInventorySettings;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.UnitInventorySettingsNode;

@Component
public class UnitInventorySettingsMapper {

    public UnitInventorySettings toDomain(UnitInventorySettingsNode node) {
        if (node == null) {
            return null;
        }
        return new UnitInventorySettings(node.getUnitId(), node.getStockCapacity(), node.getUpdatedBy(),
                node.getUpdatedByName(), node.getUpdatedAt());
    }

    public UnitInventorySettingsNode toNode(UnitInventorySettings settings) {
        if (settings == null) {
            return null;
        }
        return new UnitInventorySettingsNode(settings.getUnitId(), settings.getStockCapacity(),
                settings.getUpdatedBy(), settings.getUpdatedByName(), settings.getUpdatedAt());
    }
}
