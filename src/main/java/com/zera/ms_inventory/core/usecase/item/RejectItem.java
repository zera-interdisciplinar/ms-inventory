package com.zera.ms_inventory.core.usecase.item;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.Actor;

public interface RejectItem {
    Item execute(UUID unitId, UUID id, String reason, Actor actor);
}
