package com.zera.ms_inventory.core.domain.exception;

import java.util.UUID;

/** O id enviado pelo app ja pertence a um item de outra unidade. */
public class ItemIdInUseException extends RuntimeException {
    public ItemIdInUseException(UUID id) {
        super("Item id " + id + " is already in use");
    }
}
