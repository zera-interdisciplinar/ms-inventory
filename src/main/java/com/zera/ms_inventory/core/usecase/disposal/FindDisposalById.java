package com.zera.ms_inventory.core.usecase.disposal;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Disposal;

public interface FindDisposalById {
    Disposal execute(UUID unitId, UUID id);
}
