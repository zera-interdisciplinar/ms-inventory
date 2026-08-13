package com.zera.ms_inventory.core.domain.exception;

import java.util.UUID;

public class ModelNotFoundException extends RuntimeException {
    public ModelNotFoundException(UUID id) {
        super("Model not found with id: " + id);
    }
}
