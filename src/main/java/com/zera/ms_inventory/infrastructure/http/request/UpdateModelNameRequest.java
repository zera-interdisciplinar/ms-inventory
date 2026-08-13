package com.zera.ms_inventory.infrastructure.http.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateModelNameRequest(
        @NotBlank String name
) {}
