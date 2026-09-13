package com.zera.ms_inventory.core.domain.exception;

import java.util.UUID;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(UUID id) {
        super("Item not found with id: " + id);
    }

    private ItemNotFoundException(String message) {
        super(message);
    }

    public static ItemNotFoundException withBarcode(String barcode) {
        return new ItemNotFoundException("Item not found with barcode: " + barcode);
    }
}
