package com.zera.ms_inventory.infrastructure.http.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Capacidade de itens da unidade; é o denominador da ocupação mostrada no painel. */
public record UpdateUnitSettingsRequest(@NotNull @Min(0) Integer stockCapacity) {
}
