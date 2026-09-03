package com.zera.ms_inventory.core.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class CategoryTest {

    @Test
    void shouldCreateCategoryWithAllFields() {
        UUID id = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 4, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 8, 4, 10, 5);

        Category category = new Category(id, unitId, "Electronics", "Devices and components", createdAt, updatedAt);

        assertEquals(id, category.getId());
        assertEquals(unitId, category.getUnitId());
        assertEquals("Electronics", category.getName());
        assertEquals("Devices and components", category.getDescription());
        assertEquals(createdAt, category.getCreatedAt());
        assertEquals(updatedAt, category.getUpdatedAt());
    }

    @Test
    void shouldRenameAndUpdateDescription() {
        Category category = new Category(UUID.randomUUID(), UUID.randomUUID(), "Electronics", "Devices");
        LocalDateTime beforeUpdate = category.getUpdatedAt();

        category.rename("Hardware");
        category.updateDescription("Computer parts");

        assertEquals("Hardware", category.getName());
        assertEquals("Computer parts", category.getDescription());
        assertTrue(category.getUpdatedAt().isAfter(beforeUpdate) || category.getUpdatedAt().isEqual(beforeUpdate));
    }

    @Test
    void shouldBuildEmbeddableTextFromNameAndDescription() {
        Category category = new Category(UUID.randomUUID(), UUID.randomUUID(), "Electronics", "Devices");

        assertEquals("Electronics Devices", category.toEmbeddableText());
    }
}
