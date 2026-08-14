package com.zera.ms_inventory.infrastructure.http.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record AssignItemUnitRequest(
        @NotNull UUID unitId
) {}
