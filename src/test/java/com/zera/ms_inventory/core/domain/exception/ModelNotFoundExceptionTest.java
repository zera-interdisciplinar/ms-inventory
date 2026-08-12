package com.zera.ms_inventory.core.domain.exception;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelNotFoundExceptionTest {

    @Test
    void shouldContainIdInMessage() {
        UUID id = UUID.randomUUID();
        ModelNotFoundException exception = new ModelNotFoundException(id);

        assertTrue(exception.getMessage().contains(id.toString()));
    }
}
