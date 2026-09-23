package com.zera.ms_inventory.core.usecase.disposal;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Disposal;
import com.zera.ms_inventory.core.domain.valueobject.DestinationType;

public interface CorrectDisposalDestination {
    Disposal execute(UUID unitId, UUID id, DestinationType destination);
}
