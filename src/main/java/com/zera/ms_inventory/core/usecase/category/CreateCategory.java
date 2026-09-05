package com.zera.ms_inventory.core.usecase.category;

import java.time.LocalDateTime;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Category;

public interface CreateCategory {
    Category execute(UUID unitId, String name, String description, LocalDateTime createdAt, LocalDateTime updatedAt);
}
