package com.zera.ms_inventory.infrastructure.persistence.neo4j.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

/** Uma configuracao por unidade; o proprio unitId e a chave. */
@Node("UnitInventorySettings")
public class UnitInventorySettingsNode {

    @Id
    private UUID unitId;

    private Integer stockCapacity;

    private UUID updatedBy;

    private String updatedByName;

    private LocalDateTime updatedAt;

    public UnitInventorySettingsNode() {
    }

    public UnitInventorySettingsNode(UUID unitId, Integer stockCapacity, UUID updatedBy, String updatedByName,
                                     LocalDateTime updatedAt) {
        this.unitId = unitId;
        this.stockCapacity = stockCapacity;
        this.updatedBy = updatedBy;
        this.updatedByName = updatedByName;
        this.updatedAt = updatedAt;
    }

    public UUID getUnitId() {
        return unitId;
    }

    public Integer getStockCapacity() {
        return stockCapacity;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public String getUpdatedByName() {
        return updatedByName;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
