package com.zera.ms_inventory.infrastructure.http.request;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.DestinationType;
import com.zera.ms_inventory.core.usecase.disposal.CreateDisposalCommand;

/**
 * Registro de um descarte já realizado. {@code placeId} é o ponto na API externa de locais; sem
 * agendamento no sistema, {@code disposedAt} nunca é no futuro.
 */
public record CreateDisposalRequest(
        @NotNull DestinationType destination,
        @Size(max = 200) String placeId,
        @Size(max = 200) String placeName,
        @PastOrPresent LocalDate disposedAt,
        @Size(max = 500) String notes,
        @NotEmpty List<UUID> itemIds
) {
    public CreateDisposalCommand toCommand(UUID unitId, Actor actor) {
        return new CreateDisposalCommand(unitId, destination, placeId, placeName, disposedAt, notes, itemIds,
                actor);
    }
}
