package com.zera.ms_inventory.core.usecase.item;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.valueobject.Actor;

/** Remocao logica: o item vai para REMOVED e o gestor consegue restaurar (ZERA-247). */
public interface DeleteItem {
    void execute(UUID unitId, UUID id, Actor actor);
}
