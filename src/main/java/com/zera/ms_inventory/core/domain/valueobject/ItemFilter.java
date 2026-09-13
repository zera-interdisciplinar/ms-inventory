package com.zera.ms_inventory.core.domain.valueobject;

import java.util.UUID;

/**
 * Filtros da tela "Itens". {@code query} busca por codigo curto (prefixo), nome do item, nome do
 * modelo ou material. Campos nulos nao filtram; busca em branco e ignorada.
 */
public record ItemFilter(ItemStatus status, UUID categoryId, UUID modelId, String query) {

    public ItemFilter {
        query = query == null || query.isBlank() ? null : query.strip();
    }

    public static ItemFilter none() {
        return new ItemFilter(null, null, null, null);
    }
}
