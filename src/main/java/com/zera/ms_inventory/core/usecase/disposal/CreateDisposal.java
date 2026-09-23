package com.zera.ms_inventory.core.usecase.disposal;

import com.zera.ms_inventory.core.domain.entity.Disposal;

public interface CreateDisposal {
    Disposal execute(CreateDisposalCommand command);
}
