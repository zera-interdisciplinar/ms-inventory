package com.zera.ms_inventory.infrastructure.http.request;

import java.util.Set;

import jakarta.validation.constraints.NotEmpty;

import com.zera.ms_inventory.core.domain.valueobject.MaterialCode;

public record UpdateModelMaterialsRequest(
        @NotEmpty Set<MaterialCode> materials
) {}
