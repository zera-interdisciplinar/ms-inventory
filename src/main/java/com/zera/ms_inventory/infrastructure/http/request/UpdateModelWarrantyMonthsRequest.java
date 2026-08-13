package com.zera.ms_inventory.infrastructure.http.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateModelWarrantyMonthsRequest(
        @NotNull @Positive Integer warrantyMonths
) {}
