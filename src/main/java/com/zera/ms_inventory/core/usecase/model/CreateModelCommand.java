package com.zera.ms_inventory.core.usecase.model;

import java.util.Set;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.valueobject.MaterialCode;

public record CreateModelCommand(
        UUID unitId,
        String name,
        String manufacturer,
        Integer warrantyMonths,
        Integer expectedLifespanMonths,
        Set<MaterialCode> materials,
        Double estimatedWeightKg,
        String notes,
        UUID categoryId
) {}
