package com.zera.ms_inventory.core.usecase.disposal;

import java.util.UUID;

public interface GetDisposalReport {
    DisposalReport execute(UUID unitId, UUID disposalId);
}
