package com.zera.ms_inventory.core.domain.valueobject;

import java.util.UUID;

/** Linha bruta do agrupamento: item sem modelo/categoria chega com id e nome nulos. */
public record CategoryItemCount(UUID categoryId, String name, long itemCount) {
}
