package com.zera.ms_inventory.core.usecase.material;

import java.util.List;

import com.zera.ms_inventory.core.domain.entity.Material;

public interface ListMaterials {
    List<Material> execute();
}
