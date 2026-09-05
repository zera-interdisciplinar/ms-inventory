package com.zera.ms_inventory.core.usecase.model;

import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.exception.ModelNotFoundException;
import com.zera.ms_inventory.core.repository.ModelRepository;

@Service
public class UpdateModelHazardousMaterialsImpl implements UpdateModelHazardousMaterials {
    private final ModelRepository modelRepository;

    public UpdateModelHazardousMaterialsImpl(ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
    }

    @Override
    public Model execute(UUID unitId, UUID id, Set<String> hazardousMaterials) {
        Model model = modelRepository.findById(unitId, id)
                .orElseThrow(() -> new ModelNotFoundException(id));
        model.changeHazardousMaterials(Set.copyOf(hazardousMaterials));
        return modelRepository.save(model);
    }
}
