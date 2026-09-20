package com.zera.ms_inventory.core.usecase.item;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.exception.ItemIdInUseException;
import com.zera.ms_inventory.core.domain.exception.ModelNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.ActorRole;
import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.core.domain.valueobject.DamageType;
import com.zera.ms_inventory.core.domain.valueobject.ItemCondition;
import com.zera.ms_inventory.core.domain.valueobject.MaterialCode;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.domain.valueobject.UsageIntensity;
import com.zera.ms_inventory.core.repository.ItemRepository;
import com.zera.ms_inventory.core.repository.ModelRepository;
import com.zera.ms_inventory.core.usecase.model.CreateModel;
import com.zera.ms_inventory.core.usecase.model.CreateModelCommand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateItemImplTest {

    private static final UUID OPERATOR = UUID.randomUUID();
    private static final Actor ACTOR = new Actor(OPERATOR, ActorRole.EMPLOYEE, "Gustavo Macal");

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ModelRepository modelRepository;

    @Mock
    private CreateModel createModel;

    @Mock
    private DisplayCodeGenerator displayCodeGenerator;

    private CreateItemImpl useCase() {
        return new CreateItemImpl(itemRepository, modelRepository, createModel, displayCodeGenerator);
    }

    private CreateItemCommand command(UUID id, UUID modelId, CreateModelCommand newModel) {
        return new CreateItemCommand(id, new Barcode("7891234567890"), ItemStatus.OK, Fixtures.UNIT, modelId, newModel,
                2024, UsageIntensity.HIGH, "SN-001", LocalDate.of(2026, 8, 4), "Placa de vídeo",
                ItemCondition.SEMI_DAMAGED, true, Set.of(DamageType.OXIDATION), "Pino torto", ACTOR);
    }

    private void stubSave() {
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(displayCodeGenerator.next(Fixtures.UNIT)).thenReturn("265964");
    }

    @Test
    void shouldCreateItemLinkedToAnExistingModel() {
        UUID modelId = UUID.randomUUID();
        when(modelRepository.findById(Fixtures.UNIT, modelId))
                .thenReturn(Optional.of(Fixtures.model(modelId, Fixtures.UNIT)));
        stubSave();

        CreateItemResult result = useCase().execute(command(null, modelId, null));
        Item item = result.item();

        assertTrue(result.created());
        assertNotNull(item.getId());
        assertEquals(Fixtures.UNIT, item.getUnitId());
        assertEquals(modelId, item.getModel().getId());
        assertEquals("265964", item.getDisplayCode());
        assertEquals("Placa de vídeo", item.getName());
        assertEquals(ItemCondition.SEMI_DAMAGED, item.getCondition());
        assertEquals(Set.of(DamageType.OXIDATION), item.getDamages());
        assertEquals("Pino torto", item.getNotes());
        assertEquals(OPERATOR, item.getCreatedBy());
        assertEquals("Gustavo Macal", item.getCreatedByName());
        verify(itemRepository).save(item);
        verifyNoInteractions(createModel);
    }

    @Test
    void shouldCreateTheModelTogetherWhenItIsNew() {
        CreateModelCommand newModel = new CreateModelCommand(Fixtures.UNIT, "Placa de vídeo", "Nvidia", null, null,
                Set.of(MaterialCode.CIRCUIT_BOARD), 0.9, null, UUID.randomUUID(), ACTOR);
        Model created = Fixtures.model(Fixtures.UNIT);
        when(createModel.execute(newModel)).thenReturn(created);
        stubSave();

        Item item = useCase().execute(command(null, null, newModel)).item();

        assertSame(created, item.getModel());
        verify(modelRepository, never()).findById(any(), any());
    }

    @Test
    void shouldUseTheIdSentByTheApp() {
        UUID id = UUID.randomUUID();
        UUID modelId = UUID.randomUUID();
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.empty());
        when(itemRepository.existsAnyWithId(id)).thenReturn(false);
        when(modelRepository.findById(Fixtures.UNIT, modelId))
                .thenReturn(Optional.of(Fixtures.model(modelId, Fixtures.UNIT)));
        stubSave();

        assertEquals(id, useCase().execute(command(id, modelId, null)).item().getId());
    }

    @Test
    void shouldReturnTheExistingItemWhenTheAppResendsTheSameId() {
        UUID id = UUID.randomUUID();
        Item existing = Fixtures.item(id, Fixtures.UNIT);
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(existing));

        CreateItemResult result = useCase().execute(command(id, UUID.randomUUID(), null));

        assertFalse(result.created());
        assertSame(existing, result.item());
        verify(itemRepository, never()).save(any(Item.class));
        verifyNoInteractions(createModel, displayCodeGenerator);
    }

    @Test
    void shouldRefuseAnIdThatBelongsToAnotherUnit() {
        UUID id = UUID.randomUUID();
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.empty());
        when(itemRepository.existsAnyWithId(id)).thenReturn(true);

        CreateItemImpl useCase = useCase();
        CreateItemCommand command = command(id, UUID.randomUUID(), null);

        assertThrows(ItemIdInUseException.class, () -> useCase.execute(command));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void shouldRejectModelFromAnotherUnit() {
        UUID modelId = UUID.randomUUID();
        when(modelRepository.findById(Fixtures.UNIT, modelId)).thenReturn(Optional.empty());

        CreateItemImpl useCase = useCase();
        CreateItemCommand command = command(null, modelId, null);

        assertThrows(ModelNotFoundException.class, () -> useCase.execute(command));
        verify(itemRepository, never()).save(any(Item.class));
    }
}
