package com.zera.ms_inventory.infrastructure.http.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateItemSerialNumberRequest(
        @NotBlank String serialNumber
) {}
