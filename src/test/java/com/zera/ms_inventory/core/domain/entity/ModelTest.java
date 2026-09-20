package com.zera.ms_inventory.core.domain.entity;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.ActorRole;
import com.zera.ms_inventory.core.domain.valueobject.ApprovalStatus;
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
        Set<Material> materials = Set.of(new Material(UUID.randomUUID(), MaterialCode.BATTERY, "Pilhas e baterias",
                true, true, "guia"));
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 4, 11, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 8, 4, 11, 10);

        Model model = new Model(id, unitId, "Notebook X", "Zera", 24, 60, materials, 2.1, "Com carregador",
                category, createdAt, updatedAt);

        assertEquals(id, model.getId());
        assertEquals(unitId, model.getUnitId());
        assertEquals("Notebook X", model.getName());
        assertEquals("Zera", model.getManufacturer());
        assertEquals(24, model.getWarrantyMonths());
        assertEquals(60, model.getExpectedLifespanMonths());
        assertEquals(materials, model.getMaterials());
        assertEquals(category, model.getCategory());
        assertEquals(createdAt, model.getCreatedAt());
        assertEquals(updatedAt, model.getUpdatedAt());
    }

    @Test
    void shouldUpdateModelState() {
        UUID unitId = UUID.randomUUID();
        Model model = new Model(UUID.randomUUID(), unitId, "Notebook X", "Zera", 24, 60, Set.of(), null, null,
                category(unitId));
        LocalDateTime beforeUpdate = model.getUpdatedAt();

        model.rename("Notebook Pro");
        model.changeManufacturer("Zera Labs");
        model.changeWarrantyMonths(36);
        model.changeExpectedLifespanMonths(72);

        assertEquals("Notebook Pro", model.getName());
        assertEquals("Zera Labs", model.getManufacturer());
        assertEquals(36, model.getWarrantyMonths());
        assertEquals(72, model.getExpectedLifespanMonths());
        assertTrue(model.getUpdatedAt().isAfter(beforeUpdate) || model.getUpdatedAt().isEqual(beforeUpdate));
    }

    @Test
    void shouldBuildEmbeddableTextWithoutCategory() {
        Model model = new Model(UUID.randomUUID(), UUID.randomUUID(), "Notebook X", "Zera", 24, 60, Set.of(), null,
                null, null);

        assertEquals("Notebook X Zera", model.toEmbeddableText());
    }

    private Material material(MaterialCode code, String name, boolean hazardous) {
        return new Material(UUID.randomUUID(), code, name, true, hazardous, "guia");
    }

    @Test
    void shouldBeHazardousWhenAnyMaterialIsHazardous() {
        UUID unitId = UUID.randomUUID();
        Model model = new Model(UUID.randomUUID(), unitId, "Notebook X", "Zera", null, null,
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
        Model model = new Model(UUID.randomUUID(), unitId, "Notebook X", "Zera", 24, 60, Set.of(), null, null,
                category(unitId));

        assertThrows(IllegalArgumentException.class, () -> model.changeEstimatedWeightKg(0.0));
        assertThrows(IllegalArgumentException.class, () -> new Model(UUID.randomUUID(), unitId, "Notebook X", "Zera",
                24, 60, Set.of(), -1.0, null, category(unitId)));
    }

    @Test
    void shouldIncludeMaterialNamesInEmbeddableTextInStableOrder() {
        UUID unitId = UUID.randomUUID();
        Model model = new Model(UUID.randomUUID(), unitId, "Notebook X", "Zera", 24, 60,
                Set.of(material(MaterialCode.PLASTIC, "Plástico", false), material(MaterialCode.METAL, "Metal", false)),
                null, null, category(unitId));

        assertEquals("Notebook X Zera Notebooks Metal Plástico", model.toEmbeddableText());
    }

    @Test
    void shouldRegisterAsPendingWhenAnEmployeeCreatesAndApprovedWhenAManagerCreates() {
        UUID unitId = UUID.randomUUID();
        UUID operator = UUID.randomUUID();
        UUID manager = UUID.randomUUID();
        Model model = new Model(UUID.randomUUID(), unitId, "Notebook X", "Zera", null, null, Set.of(), null, null,
                category(unitId));

        assertEquals(ApprovalStatus.APPROVED, model.getApprovalStatus());

        model.registerBy(new Actor(operator, ActorRole.EMPLOYEE));
        assertEquals(ApprovalStatus.PENDING, model.getApprovalStatus());
        assertEquals(operator, model.getCreatedBy());
        assertNull(model.getReviewedBy());
        assertNull(model.getReviewedAt());

        model.registerBy(new Actor(manager, ActorRole.MANAGER));
        assertEquals(ApprovalStatus.APPROVED, model.getApprovalStatus());
        assertEquals(manager, model.getReviewedBy());
        assertNotNull(model.getReviewedAt());
    }

    @Test
    void shouldRestoreApprovalAndTreatMissingStatusAsApproved() {
        UUID unitId = UUID.randomUUID();
        UUID creator = UUID.randomUUID();
        Model model = new Model(UUID.randomUUID(), unitId, "Notebook X", "Zera", null, null, Set.of(), null, null,
                category(unitId));

        model.restoreApproval(ApprovalStatus.REJECTED, "Foto ilegivel", creator, null, null);
        assertEquals(ApprovalStatus.REJECTED, model.getApprovalStatus());
        assertEquals("Foto ilegivel", model.getRejectionReason());
        assertEquals(creator, model.getCreatedBy());

        model.restoreApproval(null, null, null, null, null);
        assertEquals(ApprovalStatus.APPROVED, model.getApprovalStatus());
    }

    @Test
    void shouldApproveAPendingModelAndClearTheRejection() {
        Model model = com.zera.ms_inventory.Fixtures.model(com.zera.ms_inventory.Fixtures.UNIT);
        model.registerBy(com.zera.ms_inventory.Fixtures.OPERATOR);
        model.rejectBy(com.zera.ms_inventory.Fixtures.MANAGER, "faltou o peso");

        model.approveBy(com.zera.ms_inventory.Fixtures.MANAGER);

        org.assertj.core.api.Assertions.assertThat(model.getApprovalStatus())
                .isEqualTo(com.zera.ms_inventory.core.domain.valueobject.ApprovalStatus.APPROVED);
        org.assertj.core.api.Assertions.assertThat(model.getRejectionReason()).isNull();
        org.assertj.core.api.Assertions.assertThat(model.getReviewedBy())
                .isEqualTo(com.zera.ms_inventory.Fixtures.MANAGER.userId());
    }

    /** Reaprovar nao pode reescrever quem revisou primeiro. */
    @Test
    void shouldLeaveAnAlreadyApprovedModelUntouched() {
        Model model = com.zera.ms_inventory.Fixtures.model(com.zera.ms_inventory.Fixtures.UNIT);
        model.registerBy(com.zera.ms_inventory.Fixtures.MANAGER);
        java.time.LocalDateTime reviewedAt = model.getReviewedAt();

        model.approveBy(com.zera.ms_inventory.Fixtures.OPERATOR);

        org.assertj.core.api.Assertions.assertThat(model.getReviewedBy())
                .isEqualTo(com.zera.ms_inventory.Fixtures.MANAGER.userId());
        org.assertj.core.api.Assertions.assertThat(model.getReviewedAt()).isEqualTo(reviewedAt);
    }

    @Test
    void shouldRejectWithTheReasonTrimmed() {
        Model model = com.zera.ms_inventory.Fixtures.model(com.zera.ms_inventory.Fixtures.UNIT);
        model.registerBy(com.zera.ms_inventory.Fixtures.OPERATOR);

        model.rejectBy(com.zera.ms_inventory.Fixtures.MANAGER, "  material errado  ");

        org.assertj.core.api.Assertions.assertThat(model.getApprovalStatus())
                .isEqualTo(com.zera.ms_inventory.core.domain.valueobject.ApprovalStatus.REJECTED);
        org.assertj.core.api.Assertions.assertThat(model.getRejectionReason()).isEqualTo("material errado");
        org.assertj.core.api.Assertions.assertThat(model.isPendingApproval()).isFalse();
    }
}
