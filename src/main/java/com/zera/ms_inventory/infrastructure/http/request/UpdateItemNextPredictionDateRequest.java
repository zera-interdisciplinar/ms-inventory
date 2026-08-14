package com.zera.ms_inventory.infrastructure.http.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record UpdateItemNextPredictionDateRequest(
        @NotNull LocalDateTime nextPredictionDate
) {}
