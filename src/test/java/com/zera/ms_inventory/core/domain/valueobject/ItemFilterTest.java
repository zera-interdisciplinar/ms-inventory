package com.zera.ms_inventory.core.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ItemFilterTest {

    @Test
    void shouldIgnoreBlankSearchAndTrimTheRest() {
        assertNull(new ItemFilter(null, null, null, "   ").query());
        assertEquals("placa", new ItemFilter(null, null, null, "  placa ").query());
        assertEquals(ItemFilter.none(), new ItemFilter(null, null, null, ""));
    }
}
