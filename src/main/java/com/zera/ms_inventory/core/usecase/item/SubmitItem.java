package com.zera.ms_inventory.core.usecase.item;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.Actor;

public interface SubmitItem {
    Item execute(UUID unitId, UUID id, Actor actor);
}
