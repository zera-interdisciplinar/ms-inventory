package com.zera.ms_inventory.core.usecase.category;

import java.util.List;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Category;

public interface FindAllCategories {
    List<Category> execute(UUID unitId);
}
