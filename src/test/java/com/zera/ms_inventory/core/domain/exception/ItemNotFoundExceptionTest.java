package com.zera.ms_inventory.core.domain.exception;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemNotFoundExceptionTest {

    @Test
    void shouldContainIdInMessage() {
        UUID id = UUID.randomUUID();
        ItemNotFoundException exception = new ItemNotFoundException(id);

        assertTrue(exception.getMessage().contains(id.toString()));
    }
}
