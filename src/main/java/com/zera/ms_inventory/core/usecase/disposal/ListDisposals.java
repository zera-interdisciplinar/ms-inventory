package com.zera.ms_inventory.core.usecase.disposal;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Disposal;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;

public interface ListDisposals {
    PageResult<Disposal> execute(UUID unitId, Pagination pagination);
}
