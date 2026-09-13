package com.zera.ms_inventory.core.domain.exception;

import java.util.UUID;

public class CategoryInUseException extends RuntimeException {
    public CategoryInUseException(UUID id) {
        super("Category " + id + " still has models and cannot be deleted");
    }
}
