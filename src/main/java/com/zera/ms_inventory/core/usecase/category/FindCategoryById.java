package com.zera.ms_inventory.core.usecase.category;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Category;

public interface FindCategoryById {
    Category execute(UUID unitId, UUID id);
}
