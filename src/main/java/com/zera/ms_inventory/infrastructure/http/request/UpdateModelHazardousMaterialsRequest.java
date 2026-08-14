package com.zera.ms_inventory.infrastructure.http.request;

import java.util.Set;

import jakarta.validation.constraints.NotNull;

public record UpdateModelHazardousMaterialsRequest(
        @NotNull Set<String> hazardousMaterials
) {}
