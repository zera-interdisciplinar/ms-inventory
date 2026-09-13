package com.zera.ms_inventory.core.usecase.model;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;

public interface ListModelItems {
    PageResult<Item> execute(UUID unitId, UUID modelId, Pagination pagination);
}
