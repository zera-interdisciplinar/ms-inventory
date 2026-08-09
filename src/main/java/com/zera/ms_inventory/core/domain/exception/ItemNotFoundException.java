package com.zera.ms_inventory.core.domain.exception;

import java.util.UUID;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(UUID id) {
        super("Item not found with id: " + id);
    }
}
