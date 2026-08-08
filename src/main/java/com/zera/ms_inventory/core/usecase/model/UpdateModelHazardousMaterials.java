package com.zera.ms_inventory.core.usecase.model;

import java.util.Set;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Model;

public interface UpdateModelHazardousMaterials {
    Model execute(UUID id, Set<String> hazardousMaterials);
}
