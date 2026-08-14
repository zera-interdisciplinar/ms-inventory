package com.zera.ms_inventory.infrastructure.http.request;

import jakarta.validation.constraints.NotNull;

public record UpdateItemUsageIntensityRequest(
        @NotNull Integer usageIntensity
) {}
