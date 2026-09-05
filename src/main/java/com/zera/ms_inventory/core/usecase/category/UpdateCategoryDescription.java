package com.zera.ms_inventory.core.usecase.category;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Category;

public interface UpdateCategoryDescription {
    Category execute(UUID unitId, UUID id, String description);
}
