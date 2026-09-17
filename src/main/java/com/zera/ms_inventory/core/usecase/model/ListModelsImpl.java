package com.zera.ms_inventory.core.usecase.model;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.valueobject.ApprovalStatus;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
import com.zera.ms_inventory.core.repository.ModelRepository;

@Service
public class ListModelsImpl implements ListModels {
    private final ModelRepository modelRepository;

    public ListModelsImpl(ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
    }

    @Override
    public PageResult<Model> execute(UUID unitId, ApprovalStatus approvalStatus, Pagination pagination) {
        return modelRepository.findPage(unitId, approvalStatus, pagination);
    }
}
