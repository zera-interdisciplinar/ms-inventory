package com.zera.ms_inventory.core.domain.valueobject;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Filtros da listagem de descartes no app. Campo nulo nao restringe; os que vierem combinam com E.
 * {@code query} cobre o que a tela de historico mostra sem um campo proprio: ponto, observacao,
 * quem registrou, nome do item e prefixo do codigo patrimonial.
 */
public record DisposalFilter(DestinationType destination, LocalDate disposedFrom, LocalDate disposedTo,
                             LocalDate createdFrom, LocalDate createdTo, UUID createdBy, String placeId,
                             UUID itemId, String query) {

    public DisposalFilter {
        query = query == null || query.isBlank() ? null : query.strip();
        placeId = placeId == null || placeId.isBlank() ? null : placeId.strip();
        if (disposedFrom != null && disposedTo != null && disposedFrom.isAfter(disposedTo)) {
            throw new IllegalArgumentException("disposedFrom depois de disposedTo");
        }
        if (createdFrom != null && createdTo != null && createdFrom.isAfter(createdTo)) {
            throw new IllegalArgumentException("createdFrom depois de createdTo");
        }
    }

    public static DisposalFilter none() {
        return new DisposalFilter(null, null, null, null, null, null, null, null, null);
    }
}
