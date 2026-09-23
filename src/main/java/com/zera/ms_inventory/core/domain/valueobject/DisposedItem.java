package com.zera.ms_inventory.core.domain.valueobject;

import java.util.UUID;

/**
 * Item incluido num descarte, com o peso congelado no momento em que ele saiu do estoque. O peso
 * e herdado do modelo, que pode ser corrigido depois: congelar aqui mantem os indicadores de kg
 * fieis ao que foi descartado.
 */
public record DisposedItem(UUID itemId, String displayCode, String name, Double weightKg) {

    public DisposedItem {
        if (itemId == null) {
            throw new IllegalArgumentException("itemId is required");
        }
        if (weightKg != null && weightKg < 0) {
            throw new IllegalArgumentException("weightKg cannot be negative");
        }
    }
}
