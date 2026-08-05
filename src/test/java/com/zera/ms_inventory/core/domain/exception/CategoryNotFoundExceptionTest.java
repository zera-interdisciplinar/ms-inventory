package com.zera.ms_inventory.core.domain.exception;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CategoryNotFoundExceptionTest {

    @Test
    void shouldContainIdInMessage() {
        UUID id = UUID.randomUUID();
        CategoryNotFoundException exception = new CategoryNotFoundException(id);

        assertTrue(exception.getMessage().contains(id.toString()));
    }
}
