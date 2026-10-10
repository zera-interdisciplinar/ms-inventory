package com.zera.ms_inventory.core.usecase.disposal;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.valueobject.Actor;

public interface CancelDisposal {
    void execute(UUID unitId, UUID id, Actor actor);
}
