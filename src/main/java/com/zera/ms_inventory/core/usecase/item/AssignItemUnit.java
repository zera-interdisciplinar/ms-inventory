package com.zera.ms_inventory.core.usecase.item;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Item;

public interface AssignItemUnit {
    /** Move o item da unidade {@code unitId} para {@code newUnitId}. */
    Item execute(UUID unitId, UUID id, UUID newUnitId);
}
