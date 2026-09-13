package com.zera.ms_inventory.core.domain.exception;

import java.util.UUID;

public class ModelInUseException extends RuntimeException {
    public ModelInUseException(UUID id) {
        super("Model " + id + " still has items and cannot be deleted");
    }
}
