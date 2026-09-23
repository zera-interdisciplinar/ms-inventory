package com.zera.ms_inventory.core.domain.valueobject;

import java.util.UUID;

/**
 * Filtros da tela "Itens". {@code query} busca por codigo curto (prefixo), nome do item, nome do
 * modelo ou material. Campos nulos nao filtram; busca em branco e ignorada.
 * {@code eligibleForDisposal} lista o que a maquina de estados deixa descartar.
 */
public record ItemFilter(ItemStatus status, UUID categoryId, UUID modelId, String query,
                         Boolean eligibleForDisposal) {

    public ItemFilter {
        query = query == null || query.isBlank() ? null : query.strip();
    }

    public ItemFilter(ItemStatus status, UUID categoryId, UUID modelId, String query) {
        this(status, categoryId, modelId, query, null);
    }

    public static ItemFilter none() {
        return new ItemFilter(null, null, null, null, null);
    }

    public boolean onlyEligibleForDisposal() {
        return Boolean.TRUE.equals(eligibleForDisposal);
    }
}
