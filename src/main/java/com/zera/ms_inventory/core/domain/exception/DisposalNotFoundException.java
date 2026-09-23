package com.zera.ms_inventory.core.domain.exception;

import java.util.UUID;

public class DisposalNotFoundException extends RuntimeException {
    public DisposalNotFoundException(UUID id) {
        super("Disposal not found: " + id);
    }
}
