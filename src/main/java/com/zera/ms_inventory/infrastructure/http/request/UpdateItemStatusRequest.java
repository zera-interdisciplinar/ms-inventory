package com.zera.ms_inventory.infrastructure.http.request;

import jakarta.validation.constraints.NotNull;

import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;

public record UpdateItemStatusRequest(
        @NotNull ItemStatus status
) {}
