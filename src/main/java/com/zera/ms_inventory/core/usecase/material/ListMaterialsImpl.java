package com.zera.ms_inventory.core.usecase.material;

import java.util.List;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Material;
import com.zera.ms_inventory.core.repository.MaterialRepository;

@Service
public class ListMaterialsImpl implements ListMaterials {
    private final MaterialRepository materialRepository;

    public ListMaterialsImpl(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    @Override
    public List<Material> execute() {
        return materialRepository.findAll();
    }
}
