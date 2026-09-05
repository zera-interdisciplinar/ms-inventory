package com.zera.ms_inventory.core.usecase.category;

import java.util.UUID;

public interface DeleteCategory {
    void execute(UUID unitId, UUID id);
}
