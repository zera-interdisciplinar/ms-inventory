package com.zera.ms_inventory.core.usecase.material;

import com.zera.ms_inventory.core.domain.entity.Material;
import com.zera.ms_inventory.core.domain.valueobject.MaterialCode;

public interface FindMaterialByCode {
    Material execute(MaterialCode code);
}
