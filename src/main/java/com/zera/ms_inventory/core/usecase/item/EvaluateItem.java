package com.zera.ms_inventory.core.usecase.item;

import java.util.Set;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.DamageType;
import com.zera.ms_inventory.core.domain.valueobject.ItemCondition;

public interface EvaluateItem {
    Item execute(UUID unitId, UUID id, ItemCondition condition, Boolean hasDamages, Set<DamageType> damages,
                 Actor actor);
}
