package com.zera.ms_inventory.core.domain.exception;

public class CategoryNameAlreadyExistsException extends RuntimeException {
    public CategoryNameAlreadyExistsException(String name) {
        super("Category already exists with name: " + name);
    }
}
