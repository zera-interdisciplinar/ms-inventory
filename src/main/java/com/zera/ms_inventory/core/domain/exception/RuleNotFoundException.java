package com.zera.ms_inventory.core.domain.exception;

import java.util.UUID;

public class RuleNotFoundException extends RuntimeException {
    public RuleNotFoundException(UUID id) {
        super("Rule not found with id: " + id);
    }
}
