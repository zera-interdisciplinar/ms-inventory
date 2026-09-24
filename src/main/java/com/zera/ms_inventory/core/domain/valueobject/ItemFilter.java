package com.zera.ms_inventory.core.domain.valueobject;

import java.util.UUID;

/**
 * Filtros da tela "Itens". {@code query} busca por codigo curto (prefixo), nome do item, nome do
 * modelo ou material. Campos nulos nao filtram; busca em branco e ignorada.
 * {@code eligibleForDisposal} lista o que a maquina de estados deixa descartar, e e tambem como a
 * Central de Trabalho encontra o que esta danificado e ainda sem destino.
 */
public record ItemFilter(ItemStatus status, UUID categoryId, UUID modelId, String query,
                         Boolean eligibleForDisposal, UUID createdBy, ItemCondition condition) {

    public ItemFilter {
        query = query == null || query.isBlank() ? null : query.strip();
    }

    public ItemFilter(ItemStatus status, UUID categoryId, UUID modelId, String query) {
        this(status, categoryId, modelId, query, null, null, null);
    }

    public ItemFilter(ItemStatus status, UUID categoryId, UUID modelId, String query,
                      Boolean eligibleForDisposal) {
        this(status, categoryId, modelId, query, eligibleForDisposal, null, null);
    }

    public static ItemFilter none() {
        return new ItemFilter(null, null, null, null, null, null, null);
    }

    /** Rascunhos ou reprovados de quem esta usando o app, que e o recorte da Central de Trabalho. */
    public static ItemFilter ownedBy(UUID createdBy, ItemStatus status) {
        return new ItemFilter(status, null, null, null, null, createdBy, null);
    }

    /** Danificado e ainda descartavel: e o "sem destino" da Central de Trabalho. */
    public static ItemFilter damagedWithoutDestination() {
        return new ItemFilter(null, null, null, null, true, null, ItemCondition.DAMAGED);
    }

    public boolean onlyEligibleForDisposal() {
        return Boolean.TRUE.equals(eligibleForDisposal);
    }
}
