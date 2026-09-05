package com.zera.ms_inventory.core.usecase.model;

import java.util.List;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Model;

public interface FindAllModels {
    List<Model> execute(UUID unitId);
}
