package com.zera.ms_inventory.infrastructure.http.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** O motivo vai para o histórico e para a tela do operário, então é obrigatório. */
public record RejectItemRequest(@NotBlank @Size(max = 500) String reason) {
}
