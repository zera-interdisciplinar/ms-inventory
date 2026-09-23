package com.zera.ms_inventory.infrastructure.http.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** O motivo fica no histórico para explicar por que o item saiu do estoque. */
public record StartMaintenanceRequest(@NotBlank @Size(max = 500) String reason) {
}
