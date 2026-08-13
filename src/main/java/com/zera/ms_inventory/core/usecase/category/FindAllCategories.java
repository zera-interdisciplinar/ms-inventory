package com.zera.ms_inventory.core.usecase.category;

import java.util.List;

import com.zera.ms_inventory.core.domain.entity.Category;

public interface FindAllCategories {
    List<Category> execute();
}
