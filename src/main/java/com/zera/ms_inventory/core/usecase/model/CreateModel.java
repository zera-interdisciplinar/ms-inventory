package com.zera.ms_inventory.core.usecase.model;

import com.zera.ms_inventory.core.domain.entity.Model;

public interface CreateModel {
    Model execute(CreateModelCommand command);
}
