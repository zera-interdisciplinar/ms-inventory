package com.zera.ms_inventory.core.usecase.model;

import java.util.Set;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.valueobject.MaterialCode;

public interface UpdateModelMaterials {
    Model execute(UUID unitId, UUID id, Set<MaterialCode> materials);
}
