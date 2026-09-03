package com.zera.ms_inventory.core.domain.exception;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleNotFoundExceptionTest {

    @Test
    void shouldContainIdInMessage() {
        UUID id = UUID.randomUUID();
        RuleNotFoundException exception = new RuleNotFoundException(id);

        assertTrue(exception.getMessage().contains(id.toString()));
    }
}
