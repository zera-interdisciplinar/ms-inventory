package com.zera.ms_inventory.core.domain.exception;

public class ModelNameAlreadyExistsException extends RuntimeException {
    public ModelNameAlreadyExistsException(String name) {
        super("Model already exists with name: " + name);
    }
}
