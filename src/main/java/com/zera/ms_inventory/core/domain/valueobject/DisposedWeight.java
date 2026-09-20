package com.zera.ms_inventory.core.domain.valueobject;

import java.time.LocalDate;
import java.util.List;

/**
 * Peso congelado de um item descartado, com os materiais do modelo dele. E a materia-prima dos
 * indicadores: vem direto da aresta INCLUDES, entao reflete o que foi descartado de fato.
 */
public record DisposedWeight(DestinationType destination, LocalDate disposedAt, Double weightKg,
                             List<MaterialCode> materials) {

    public DisposedWeight {
        materials = materials == null ? List.of() : List.copyOf(materials);
    }

    public double weightOrZero() {
        return weightKg != null ? weightKg : 0.0;
    }
}
