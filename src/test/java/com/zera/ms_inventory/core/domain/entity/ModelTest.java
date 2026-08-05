package com.zera.ms_inventory.core.domain.entity;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class ModelTest {

    @Test
    void shouldCreateModelWithAllFields() {
        UUID id = UUID.randomUUID();
        Set<String> hazardousMaterials = Set.of("Lithium", "Mercury");
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 4, 11, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 8, 4, 11, 10);

        Model model = new Model(id, "Notebook X", "Zera", 24, 60, hazardousMaterials, createdAt, updatedAt);

        assertEquals(id, model.getId());
        assertEquals("Notebook X", model.getName());
        assertEquals("Zera", model.getManufacturer());
        assertEquals(24, model.getWarrantyMonths());
        assertEquals(60, model.getExpectedLifespanMonths());
        assertEquals(hazardousMaterials, model.getHazardousMaterials());
        assertEquals(createdAt, model.getCreatedAt());
        assertEquals(updatedAt, model.getUpdatedAt());
    }

    @Test
    void shouldUpdateModelState() {
        Model model = new Model(UUID.randomUUID(), "Notebook X", "Zera", 24, 60, Set.of("Lithium"));
        LocalDateTime beforeUpdate = model.getUpdatedAt();

        model.rename("Notebook Pro");
        model.changeManufacturer("Zera Labs");
        model.changeWarrantyMonths(36);
        model.changeExpectedLifespanMonths(72);
        model.changeHazardousMaterials(Set.of("Lithium", "Cobalt"));

        assertEquals("Notebook Pro", model.getName());
        assertEquals("Zera Labs", model.getManufacturer());
        assertEquals(36, model.getWarrantyMonths());
        assertEquals(72, model.getExpectedLifespanMonths());
        assertEquals(Set.of("Lithium", "Cobalt"), model.getHazardousMaterials());
        assertTrue(model.getUpdatedAt().isAfter(beforeUpdate) || model.getUpdatedAt().isEqual(beforeUpdate));
    }
}
