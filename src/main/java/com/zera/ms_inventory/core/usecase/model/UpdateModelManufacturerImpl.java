package com.zera.ms_inventory.core.usecase.model;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.exception.ModelNotFoundException;
import com.zera.ms_inventory.core.repository.ModelRepository;

@Service
public class UpdateModelManufacturerImpl implements UpdateModelManufacturer {
    private final ModelRepository modelRepository;

    public UpdateModelManufacturerImpl(ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
    }

    @Override
    public Model execute(UUID unitId, UUID id, String manufacturer) {
        Model model = modelRepository.findById(unitId, id)
                .orElseThrow(() -> new ModelNotFoundException(id));
        model.changeManufacturer(manufacturer);
        return modelRepository.save(model);
    }
}
