package com.zera.ms_inventory.core.usecase.item;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Event;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;

public interface ListItemEvents {
    PageResult<Event> execute(UUID unitId, UUID itemId, Pagination pagination);
}
