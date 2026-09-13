package com.zera.ms_inventory.infrastructure.http.response;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Model;

public record ModelResponse(
        UUID id,
        UUID unitId,
        String name,
        String manufacturer,
        Integer warrantyMonths,
        Integer expectedLifespanMonths,
        List<MaterialResponse> materials,
        boolean hazardous,
        Double estimatedWeightKg,
        String notes,
        CategoryResponse category,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ModelResponse from(Model model) {
        if (model == null) {
            return null;
        }
        List<MaterialResponse> materials = model.getMaterials().stream()
                .map(MaterialResponse::from)
                .sorted(Comparator.comparing(MaterialResponse::name))
                .toList();
        return new ModelResponse(model.getId(), model.getUnitId(), model.getName(), model.getManufacturer(),
                model.getWarrantyMonths(), model.getExpectedLifespanMonths(), materials, model.isHazardous(),
                model.getEstimatedWeightKg(), model.getNotes(), CategoryResponse.from(model.getCategory()),
                model.getCreatedAt(), model.getUpdatedAt());
    }
}
