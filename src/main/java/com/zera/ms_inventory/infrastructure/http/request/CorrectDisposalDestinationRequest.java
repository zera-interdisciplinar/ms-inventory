package com.zera.ms_inventory.infrastructure.http.request;

import jakarta.validation.constraints.NotNull;

import com.zera.ms_inventory.core.domain.valueobject.DestinationType;

/** Correção do destino informado por engano; os itens seguem descartados. */
public record CorrectDisposalDestinationRequest(@NotNull DestinationType destination) {
}
