package com.zera.ms_inventory.core.domain.valueobject;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaginationTest {

    @Test
    void shouldRejectNegativePage() {
        assertThrows(IllegalArgumentException.class, () -> new Pagination(-1, 20));
    }

    @Test
    void shouldRejectSizeOutsideTheAllowedRange() {
        assertThrows(IllegalArgumentException.class, () -> new Pagination(0, 0));
        assertThrows(IllegalArgumentException.class, () -> new Pagination(0, Pagination.MAX_SIZE + 1));
    }

    @Test
    void shouldComputeTotalPagesAndMapContent() {
        PageResult<Integer> page = new PageResult<>(List.of(1, 2), 0, 2, 5);

        PageResult<String> mapped = page.map(String::valueOf);

        assertEquals(3, mapped.totalPages());
        assertEquals(List.of("1", "2"), mapped.content());
        assertEquals(5, mapped.totalElements());
    }

    @Test
    void shouldHaveZeroPagesWhenEmpty() {
        assertEquals(0, new PageResult<>(List.of(), 0, 20, 0).totalPages());
    }
}
