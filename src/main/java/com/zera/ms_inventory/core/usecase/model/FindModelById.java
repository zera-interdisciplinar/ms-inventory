package com.zera.ms_inventory.core.usecase.model;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Model;

public interface FindModelById {
    Model execute(UUID unitId, UUID id);
}
