package com.zera.ms_inventory.core.usecase.model;

import java.util.Set;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Model;

public interface CreateModel {
    Model execute(UUID unitId, String name, String manufacturer, Integer warrantyMonths,
                  Integer expectedLifespanMonths, Set<String> hazardousMaterials, UUID categoryId);
}
