package com.zera.ms_inventory.core.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.valueobject.Actor;

/**
 * Configuracao de estoque da unidade. A capacidade e o denominador da ocupacao mostrada no painel;
 * enquanto ninguem a define, a ocupacao nao tem como ser calculada e fica indisponivel.
 */
public class UnitInventorySettings {

    private final UUID unitId;
    private Integer stockCapacity;
    private UUID updatedBy;
    private String updatedByName;
    private LocalDateTime updatedAt;

    public UnitInventorySettings(UUID unitId, Integer stockCapacity, UUID updatedBy, String updatedByName,
                                 LocalDateTime updatedAt) {
        if (unitId == null) {
            throw new IllegalArgumentException("unitId is required");
        }
        validateCapacity(stockCapacity);
        this.unitId = unitId;
        this.stockCapacity = stockCapacity;
        this.updatedBy = updatedBy;
        this.updatedByName = updatedByName;
        this.updatedAt = updatedAt;
    }

    /** Unidade que ainda nao configurou o estoque: sem capacidade, sem quem editou. */
    public static UnitInventorySettings notConfigured(UUID unitId) {
        return new UnitInventorySettings(unitId, null, null, null, null);
    }

    public void changeCapacity(Integer stockCapacity, Actor actor) {
        validateCapacity(stockCapacity);
        this.stockCapacity = stockCapacity;
        this.updatedBy = actor != null ? actor.userId() : null;
        this.updatedByName = actor != null ? actor.name() : null;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isConfigured() {
        return stockCapacity != null;
    }

    /**
     * Ocupacao em porcentagem, ou vazio enquanto a capacidade nao estiver definida. Passar de 100%
     * e possivel e informativo: significa estoque acima do que a unidade comporta.
     */
    public java.util.OptionalDouble occupancyPercent(long items) {
        if (stockCapacity == null || stockCapacity == 0) {
            return java.util.OptionalDouble.empty();
        }
        return java.util.OptionalDouble.of(items * 100.0 / stockCapacity);
    }

    private static void validateCapacity(Integer stockCapacity) {
        if (stockCapacity != null && stockCapacity < 0) {
            throw new IllegalArgumentException("stockCapacity cannot be negative");
        }
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
