package com.zera.ms_inventory.infrastructure.http.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record UpdateItemAcquiredAtRequest(
        @NotNull LocalDate acquiredAt
) {}
