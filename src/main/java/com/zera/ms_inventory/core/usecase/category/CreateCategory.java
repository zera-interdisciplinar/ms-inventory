package com.zera.ms_inventory.core.usecase.category;

import java.time.LocalDateTime;

import com.zera.ms_inventory.core.domain.entity.Category;

public interface CreateCategory {
    Category execute(String name, String description, LocalDateTime createdAt, LocalDateTime updatedAt);
}
