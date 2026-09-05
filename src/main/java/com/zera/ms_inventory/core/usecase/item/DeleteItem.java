package com.zera.ms_inventory.core.usecase.item;

import java.util.UUID;

public interface DeleteItem {
    void execute(UUID unitId, UUID id);
}
