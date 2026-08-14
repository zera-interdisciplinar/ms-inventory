package com.zera.ms_inventory.infrastructure.http.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateModelExpectedLifespanMonthsRequest(
        @NotNull @Positive Integer expectedLifespanMonths
) {}
