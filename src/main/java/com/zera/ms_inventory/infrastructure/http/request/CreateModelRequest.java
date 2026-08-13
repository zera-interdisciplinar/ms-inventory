package com.zera.ms_inventory.infrastructure.http.request;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateModelRequest(
        @NotBlank String name,
        @NotBlank String manufacturer,
        @NotNull @Positive Integer warrantyMonths,
        @NotNull @Positive Integer expectedLifespanMonths,
        @NotNull Set<String> hazardousMaterials
) {}
