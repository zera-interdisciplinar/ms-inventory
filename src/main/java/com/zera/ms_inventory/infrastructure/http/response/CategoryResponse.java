package com.zera.ms_inventory.infrastructure.http.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Category;

public record CategoryResponse(
        UUID id,
        UUID unitId,
        String name,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CategoryResponse from(Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryResponse(category.getId(), category.getUnitId(), category.getName(),
                category.getDescription(), category.getCreatedAt(), category.getUpdatedAt());
    }
}
