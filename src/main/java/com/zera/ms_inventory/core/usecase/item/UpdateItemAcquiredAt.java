package com.zera.ms_inventory.core.usecase.item;

import java.time.LocalDate;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Item;

public interface UpdateItemAcquiredAt {
    Item execute(UUID id, LocalDate acquiredAt);
}
