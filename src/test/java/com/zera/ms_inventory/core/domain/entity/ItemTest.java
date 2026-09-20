package com.zera.ms_inventory.core.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.ActorRole;
import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.core.domain.valueobject.DamageType;
import com.zera.ms_inventory.core.domain.valueobject.EventType;
import com.zera.ms_inventory.core.domain.valueobject.ItemCondition;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.domain.exception.InvalidItemTransitionException;

class ItemTest {

    private Model model(UUID unitId) {
        Category category = new Category(UUID.randomUUID(), unitId, "Notebooks", "Portable computers");
        return new Model(UUID.randomUUID(), unitId, "Notebook X", "Zera", 24, 60, Set.of(), null, null, category);
    }

    @Test
    void shouldCreateItemWithAllFields() {
        UUID id = UUID.randomUUID();
        Barcode barcode = new Barcode("7891234567890");
        UUID unitId = UUID.randomUUID();
        Model model = model(unitId);
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 4, 12, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 8, 4, 12, 5);
        LocalDateTime lastEventAt = LocalDateTime.of(2026, 8, 4, 12, 10);
        LocalDate predictedFailureDate = LocalDate.of(2026, 11, 10);
        LocalDate acquiredAt = LocalDate.of(2026, 8, 4);

        Item item = new Item(id, barcode, ItemStatus.IN_STOCK, unitId, model, createdAt, updatedAt, lastEventAt,
                predictedFailureDate, 2024, 6, "SN-001", acquiredAt);

        assertEquals(id, item.getId());
        assertEquals(barcode, item.getBarcode());
        assertEquals(ItemStatus.IN_STOCK, item.getStatus());
        assertEquals(unitId, item.getUnitId());
        assertEquals(model, item.getModel());
        assertEquals(createdAt, item.getCreatedAt());
        assertEquals(updatedAt, item.getUpdatedAt());
        assertEquals(lastEventAt, item.getLastEventAt());
        assertEquals(predictedFailureDate, item.getPredictedFailureDate());
        assertEquals(2024, item.getManufacturingYear());
        assertEquals(6, item.getUsageIntensity());
        assertEquals("SN-001", item.getSerialNumber());
        assertEquals(acquiredAt, item.getAcquiredAt());
    }

    @Test
    void shouldUpdateItemState() {
        UUID unitId = UUID.randomUUID();
        Item item = new Item(
                UUID.randomUUID(),
                new Barcode("7891234567890"),
                ItemStatus.IN_STOCK,
                unitId,
                model(unitId),
                LocalDateTime.of(2026, 8, 4, 12, 10),
                LocalDate.of(2026, 11, 10),
                2024,
                6,
                "SN-001",
                LocalDate.of(2026, 8, 4)
        );

        LocalDateTime beforeUpdate = item.getUpdatedAt();

        UUID newUnitId = UUID.randomUUID();
        LocalDate newPredictionDate = LocalDate.of(2026, 12, 20);
        LocalDate newAcquiredAt = LocalDate.of(2026, 8, 5);

        item.transitionTo(ItemStatus.IN_MAINTENANCE, EventType.MAINTENANCE_STARTED, null, null);
        item.assignUnit(newUnitId);
        item.updateSerialNumber("SN-002");
        item.updateAcquiredAt(newAcquiredAt);
        item.recordPrediction(newPredictionDate);
        item.updateManufacturingYear(2025);
        item.updateUsageIntensity(9);

        assertEquals(ItemStatus.IN_MAINTENANCE, item.getStatus());
        assertEquals(newUnitId, item.getUnitId());
        assertEquals("SN-002", item.getSerialNumber());
        assertEquals(newAcquiredAt, item.getAcquiredAt());
        assertEquals(newPredictionDate, item.getPredictedFailureDate());
        org.junit.jupiter.api.Assertions.assertNotNull(item.getPredictionUpdatedAt());
        assertEquals(2025, item.getManufacturingYear());
        assertEquals(9, item.getUsageIntensity());
        assertTrue(item.getUpdatedAt().isAfter(beforeUpdate) || item.getUpdatedAt().isEqual(beforeUpdate));
    }

    @Test
    void shouldDescribeTheItemAndRecordWhoRegisteredIt() {
        UUID unitId = UUID.randomUUID();
        UUID operator = UUID.randomUUID();
        Item item = new Item(UUID.randomUUID(), new Barcode("111111-J"), ItemStatus.IN_STOCK, unitId, model(unitId),
                null, null, null, null, null, null);

        item.describe("Placa de vídeo", ItemCondition.SEMI_DAMAGED, true,
                Set.of(DamageType.BROKEN_SCREEN, DamageType.OXIDATION), "Pino torto");
        item.registerBy(new Actor(operator, ActorRole.EMPLOYEE, "Gustavo Macal"));

        assertEquals("Placa de vídeo", item.getName());
        assertEquals(ItemCondition.SEMI_DAMAGED, item.getCondition());
        assertEquals(Boolean.TRUE, item.getHasDamages());
        assertEquals(Set.of(DamageType.BROKEN_SCREEN, DamageType.OXIDATION), item.getDamages());
        assertEquals("Pino torto", item.getNotes());
        assertEquals(operator, item.getCreatedBy());
        assertEquals("Gustavo Macal", item.getCreatedByName());
    }

    @Test
    void shouldRejectDamagesWhenTheItemHasNoDamages() {
        UUID unitId = UUID.randomUUID();
        Item item = new Item(UUID.randomUUID(), new Barcode("111111-J"), ItemStatus.IN_STOCK, unitId, model(unitId),
                null, null, null, null, null, null);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> item.describe("Mouse", ItemCondition.USED, false, Set.of(DamageType.OTHER), null));
    }

    @Test
    void shouldAllowUnansweredDamagesAndKeepDamagesEmpty() {
        UUID unitId = UUID.randomUUID();
        Item item = new Item(UUID.randomUUID(), new Barcode("111111-J"), ItemStatus.IN_STOCK, unitId, model(unitId),
                null, null, null, null, null, null);

        item.describe("Mouse", null, null, null, null);

        assertEquals(null, item.getHasDamages());
        assertEquals(Set.of(), item.getDamages());
    }

    @Test
    void shouldReturnTheEventOfTheTransitionAndMoveTheStatus() {
        UUID unitId = UUID.randomUUID();
        Item item = new Item(UUID.randomUUID(), new Barcode("111111-J"), ItemStatus.IN_STOCK, unitId, model(unitId),
                null, null, null, null, null, null);

        Event event = item.transitionTo(ItemStatus.IN_MAINTENANCE, EventType.MAINTENANCE_STARTED,
                "Tela piscando", new Actor(UUID.randomUUID(), ActorRole.EMPLOYEE, "Gustavo"));

        assertEquals(ItemStatus.IN_MAINTENANCE, item.getStatus());
        assertEquals(ItemStatus.IN_STOCK, event.getFromStatus());
        assertEquals(ItemStatus.IN_MAINTENANCE, event.getToStatus());
        assertEquals(EventType.MAINTENANCE_STARTED, event.getType());
        assertEquals("Tela piscando", event.getReason());
        assertEquals(item.getId(), event.getItemId());
        assertEquals(unitId, event.getUnitId());
        assertEquals("Gustavo", event.getActorName());
        org.junit.jupiter.api.Assertions.assertNotNull(item.getLastEventAt());
    }

    @Test
    void shouldRefuseATransitionOutsideTheStateMachineAndKeepTheStatus() {
        UUID unitId = UUID.randomUUID();
        Item item = new Item(UUID.randomUUID(), new Barcode("111111-J"), ItemStatus.IN_STOCK, unitId, model(unitId),
                null, null, null, null, null, null);

        org.junit.jupiter.api.Assertions.assertThrows(InvalidItemTransitionException.class,
                () -> item.transitionTo(ItemStatus.AWAITING_EVALUATION, EventType.EVALUATED, null, null));
        assertEquals(ItemStatus.IN_STOCK, item.getStatus());
    }

    /** O descarte encerra a vida do item: nem a remocao logica sai de la. */
    @Test
    void shouldRefuseAnyTransitionOutOfDisposed() {
        UUID unitId = UUID.randomUUID();
        Item item = new Item(UUID.randomUUID(), new Barcode("111111-J"), ItemStatus.IN_STOCK, unitId, model(unitId),
                null, null, null, null, null, null);
        item.transitionTo(ItemStatus.DISPOSED, EventType.DISPOSED, null, null);

        org.junit.jupiter.api.Assertions.assertThrows(InvalidItemTransitionException.class,
                () -> item.transitionTo(ItemStatus.REMOVED, EventType.REMOVED, null, null));
        org.junit.jupiter.api.Assertions.assertThrows(InvalidItemTransitionException.class,
                () -> item.transitionTo(ItemStatus.IN_STOCK, EventType.RESTORED, null, null));
    }

    /** restoreStatus e da persistencia: reidrata sem passar pela maquina de estados. */
    @Test
    void shouldRestoreTheStatusWithoutValidatingTheTransition() {
        UUID unitId = UUID.randomUUID();
        Item item = new Item(UUID.randomUUID(), new Barcode("111111-J"), ItemStatus.IN_STOCK, unitId, model(unitId),
                null, null, null, null, null, null);

        item.restoreStatus(ItemStatus.AWAITING_EVALUATION);

        assertEquals(ItemStatus.AWAITING_EVALUATION, item.getStatus());
    }

    @Test
    void shouldRejectAUsageIntensityOutsideTheZeroToTenScale() {
        UUID unitId = UUID.randomUUID();
        Item item = new Item(UUID.randomUUID(), new Barcode("111111-J"), ItemStatus.IN_STOCK, unitId, model(unitId),
                null, null, null, null, null, null);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> item.updateUsageIntensity(11));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> item.updateUsageIntensity(-1));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Item(UUID.randomUUID(), new Barcode("222222-J"), ItemStatus.IN_STOCK, unitId, model(unitId),
                        null, 2024, 42, null, null));
    }

    @Test
    void shouldAcceptTheEndsOfTheUsageIntensityScaleAndNoAnswer() {
        UUID unitId = UUID.randomUUID();
        Item item = new Item(UUID.randomUUID(), new Barcode("111111-J"), ItemStatus.IN_STOCK, unitId, model(unitId),
                null, null, null, null, null, null);

        item.updateUsageIntensity(0);
        assertEquals(0, item.getUsageIntensity());

        item.updateUsageIntensity(10);
        assertEquals(10, item.getUsageIntensity());

        item.updateUsageIntensity(null);
        assertEquals(null, item.getUsageIntensity());
    }

    @Test
    void shouldRejectAnImplausibleManufacturingYear() {
        UUID unitId = UUID.randomUUID();
        Item item = new Item(UUID.randomUUID(), new Barcode("111111-J"), ItemStatus.IN_STOCK, unitId, model(unitId),
                null, null, null, null, null, null);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> item.updateManufacturingYear(1900));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> item.updateManufacturingYear(LocalDate.now().getYear() + 1));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Item(UUID.randomUUID(), new Barcode("222222-J"), ItemStatus.IN_STOCK, unitId, model(unitId),
                        null, 1800, null, null, null));
    }

    @Test
    void shouldAssignTheDisplayCodeOnlyOnceAndWithSixDigits() {
        UUID unitId = UUID.randomUUID();
        Item item = new Item(UUID.randomUUID(), new Barcode("111111-J"), ItemStatus.IN_STOCK, unitId, model(unitId),
                null, null, null, null, null, null);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> item.assignDisplayCode("12AB"));
        item.assignDisplayCode("265964");

        assertEquals("265964", item.getDisplayCode());
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> item.assignDisplayCode("481516"));
    }

    @Test
    void shouldAttachAPhotoKey() {
        UUID unitId = UUID.randomUUID();
        Item item = new Item(UUID.randomUUID(), new Barcode("111111-J"), ItemStatus.IN_STOCK, unitId, model(unitId),
                null, null, null, null, null, null);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> item.attachPhoto(" "));
        item.attachPhoto("units/u/items/i/p.jpg");

        assertEquals("units/u/items/i/p.jpg", item.getPhotoKey());
    }
}
