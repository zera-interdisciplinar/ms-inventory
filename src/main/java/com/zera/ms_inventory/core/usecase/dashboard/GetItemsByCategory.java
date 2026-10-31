package com.zera.ms_inventory.core.usecase.dashboard;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.valueobject.CategoryBreakdown;

public interface GetItemsByCategory {
    CategoryBreakdown execute(UUID unitId);
}
