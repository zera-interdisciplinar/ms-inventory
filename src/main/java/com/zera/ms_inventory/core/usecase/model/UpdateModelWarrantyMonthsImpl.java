package com.zera.ms_inventory.core.usecase.model;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.exception.ModelNotFoundException;
import com.zera.ms_inventory.core.repository.ModelRepository;

@Service
public class UpdateModelWarrantyMonthsImpl implements UpdateModelWarrantyMonths {
    private final ModelRepository modelRepository;

    public UpdateModelWarrantyMonthsImpl(ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
    }

    @Override
    public Model execute(UUID id, Integer warrantyMonths) {
        Model model = modelRepository.findById(id)
                .orElseThrow(() -> new ModelNotFoundException(id));
        model.changeWarrantyMonths(warrantyMonths);
        return modelRepository.save(model);
    }
}
