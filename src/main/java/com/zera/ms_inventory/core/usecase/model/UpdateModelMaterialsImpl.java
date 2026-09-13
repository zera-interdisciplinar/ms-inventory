package com.zera.ms_inventory.core.usecase.model;

import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.exception.ModelNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.MaterialCode;
import com.zera.ms_inventory.core.repository.ModelRepository;
import com.zera.ms_inventory.core.usecase.material.MaterialResolver;

@Service
public class UpdateModelMaterialsImpl implements UpdateModelMaterials {
    private final ModelRepository modelRepository;
    private final MaterialResolver materialResolver;

    public UpdateModelMaterialsImpl(ModelRepository modelRepository, MaterialResolver materialResolver) {
        this.modelRepository = modelRepository;
        this.materialResolver = materialResolver;
    }

    @Override
    public Model execute(UUID unitId, UUID id, Set<MaterialCode> materials) {
        Model model = modelRepository.findById(unitId, id)
                .orElseThrow(() -> new ModelNotFoundException(id));
        model.changeMaterials(materialResolver.resolve(materials));
        return modelRepository.save(model);
    }
}
