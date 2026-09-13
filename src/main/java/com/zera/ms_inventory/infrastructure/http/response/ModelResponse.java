package com.zera.ms_inventory.infrastructure.http.response;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Model;

public record ModelResponse(
        UUID id,
        UUID unitId,
        String name,
        String manufacturer,
        Integer warrantyMonths,
        Integer expectedLifespanMonths,
        Set<String> hazardousMaterials,
        CategoryResponse category,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ModelResponse from(Model model) {
        if (model == null) {
            return null;
        }
        return new ModelResponse(model.getId(), model.getUnitId(), model.getName(), model.getManufacturer(),
                model.getWarrantyMonths(), model.getExpectedLifespanMonths(), model.getHazardousMaterials(),
                CategoryResponse.from(model.getCategory()), model.getCreatedAt(), model.getUpdatedAt());
    }
}
