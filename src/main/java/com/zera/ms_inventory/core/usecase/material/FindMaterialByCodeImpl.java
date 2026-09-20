package com.zera.ms_inventory.core.usecase.material;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Material;
import com.zera.ms_inventory.core.domain.exception.MaterialNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.MaterialCode;
import com.zera.ms_inventory.core.repository.MaterialRepository;

@Service
public class FindMaterialByCodeImpl implements FindMaterialByCode {
    private final MaterialRepository materialRepository;

    public FindMaterialByCodeImpl(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    @Override
    public Material execute(MaterialCode code) {
        return materialRepository.findByCode(code)
                .orElseThrow(() -> new MaterialNotFoundException(code));
    }
}
