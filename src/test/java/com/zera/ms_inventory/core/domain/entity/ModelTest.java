package com.zera.ms_inventory.core.domain.entity;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.zera.ms_inventory.core.domain.valueobject.MaterialCode;

class ModelTest {

    private Category category(UUID unitId) {
        return new Category(UUID.randomUUID(), unitId, "Notebooks", "Portable computers");
    }

    @Test
    void shouldCreateModelWithAllFields() {
        UUID id = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        Category category = category(unitId);
        Set<String> hazardousMaterials = Set.of("Lithium", "Mercury");
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 4, 11, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 8, 4, 11, 10);

        Model model = new Model(id, unitId, "Notebook X", "Zera", 24, 60, hazardousMaterials, category, createdAt, updatedAt);

        assertEquals(id, model.getId());
        assertEquals(unitId, model.getUnitId());
        assertEquals("Notebook X", model.getName());
        assertEquals("Zera", model.getManufacturer());
        assertEquals(24, model.getWarrantyMonths());
        assertEquals(60, model.getExpectedLifespanMonths());
        assertEquals(hazardousMaterials, model.getHazardousMaterials());
        assertEquals(category, model.getCategory());
        assertEquals(createdAt, model.getCreatedAt());
        assertEquals(updatedAt, model.getUpdatedAt());
    }

    @Test
    void shouldUpdateModelState() {
        UUID unitId = UUID.randomUUID();
        Model model = new Model(UUID.randomUUID(), unitId, "Notebook X", "Zera", 24, 60, Set.of("Lithium"), category(unitId));
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

    @Test
    void shouldBuildEmbeddableTextWithStableHazmatOrder() {
        UUID unitId = UUID.randomUUID();
        Model model = new Model(UUID.randomUUID(), unitId, "Notebook X", "Zera", 24, 60,
                Set.of("Mercury", "Cobalt", "Lithium"), category(unitId));

        // ordenado: senao o texto mudaria a cada boot e re-embedaria sem necessidade
        assertEquals("Notebook X Zera Notebooks Cobalt Lithium Mercury", model.toEmbeddableText());
    }

    @Test
    void shouldBuildEmbeddableTextWithoutCategory() {
        Model model = new Model(UUID.randomUUID(), UUID.randomUUID(), "Notebook X", "Zera", 24, 60, Set.of(), null);

        assertEquals("Notebook X Zera", model.toEmbeddableText());
    }

    private Material material(MaterialCode code, String name, boolean hazardous) {
        return new Material(UUID.randomUUID(), code, name, true, hazardous, "guia");
    }

    @Test
    void shouldBeHazardousWhenAnyMaterialIsHazardous() {
        UUID unitId = UUID.randomUUID();
        Model model = new Model(UUID.randomUUID(), unitId, "Notebook X", "Zera", null, null, Set.of(),
                Set.of(material(MaterialCode.PLASTIC, "Plástico", false)), 2.1, "Com carregador", category(unitId));

        assertFalse(model.isHazardous());
        assertEquals(2.1, model.getEstimatedWeightKg());
        assertEquals("Com carregador", model.getNotes());

        model.changeMaterials(Set.of(material(MaterialCode.PLASTIC, "Plástico", false),
                material(MaterialCode.BATTERY, "Pilhas e baterias", true)));
        model.changeEstimatedWeightKg(2.4);
        model.changeNotes(null);

        assertTrue(model.isHazardous());
        assertEquals(2, model.getMaterials().size());
        assertEquals(2.4, model.getEstimatedWeightKg());
        assertEquals(null, model.getNotes());
    }

    @Test
    void shouldRejectNonPositiveWeight() {
        UUID unitId = UUID.randomUUID();
        Model model = new Model(UUID.randomUUID(), unitId, "Notebook X", "Zera", 24, 60, Set.of(), category(unitId));

        assertThrows(IllegalArgumentException.class, () -> model.changeEstimatedWeightKg(0.0));
        assertThrows(IllegalArgumentException.class, () -> new Model(UUID.randomUUID(), unitId, "Notebook X", "Zera",
                24, 60, Set.of(), Set.of(), -1.0, null, category(unitId)));
    }

    @Test
    void shouldIncludeMaterialNamesInEmbeddableTextInStableOrder() {
        UUID unitId = UUID.randomUUID();
        Model model = new Model(UUID.randomUUID(), unitId, "Notebook X", "Zera", 24, 60, Set.of(),
                Set.of(material(MaterialCode.PLASTIC, "Plástico", false), material(MaterialCode.METAL, "Metal", false)),
                null, null, category(unitId));

        assertEquals("Notebook X Zera Notebooks Metal Plástico", model.toEmbeddableText());
    }
}
