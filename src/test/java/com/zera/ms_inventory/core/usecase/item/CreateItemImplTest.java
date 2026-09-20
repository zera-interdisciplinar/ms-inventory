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
import com.zera.ms_inventory.core.domain.exception.ModelNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.ActorRole;
import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.core.domain.valueobject.DamageType;
import com.zera.ms_inventory.core.domain.valueobject.ItemCondition;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.domain.valueobject.UsageIntensity;
import com.zera.ms_inventory.core.repository.ItemRepository;
import com.zera.ms_inventory.core.repository.ModelRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateItemImplTest {

    private static final UUID OPERATOR = UUID.randomUUID();

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ModelRepository modelRepository;

    private CreateItemCommand command(UUID modelId, UUID unitId) {
        return new CreateItemCommand(new Barcode("7891234567890"), ItemStatus.OK, unitId, modelId,
                2024, UsageIntensity.HIGH, "SN-001", LocalDate.of(2026, 8, 4), "Placa de vídeo",
                ItemCondition.SEMI_DAMAGED, true, Set.of(DamageType.OXIDATION), "Pino torto",
                new Actor(OPERATOR, ActorRole.EMPLOYEE, "Gustavo Macal"));
    }

    @Test
    void shouldCreateItemLinkedToTheModel() {
        UUID modelId = UUID.randomUUID();
        when(modelRepository.findById(Fixtures.UNIT, modelId))
                .thenReturn(Optional.of(Fixtures.model(modelId, Fixtures.UNIT)));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateItemImpl useCase = new CreateItemImpl(itemRepository, modelRepository);
        Item result = useCase.execute(command(modelId, Fixtures.UNIT));

        assertNotNull(result.getId());
        assertEquals(Fixtures.UNIT, result.getUnitId());
        assertEquals(modelId, result.getModel().getId());
        assertEquals("Placa de vídeo", result.getName());
        assertEquals(ItemCondition.SEMI_DAMAGED, result.getCondition());
        assertEquals(Set.of(DamageType.OXIDATION), result.getDamages());
        assertEquals("Pino torto", result.getNotes());
        assertEquals(OPERATOR, result.getCreatedBy());
        assertEquals("Gustavo Macal", result.getCreatedByName());
        verify(itemRepository).save(result);
    }

    @Test
    void shouldRejectModelFromAnotherUnit() {
        UUID modelId = UUID.randomUUID();
        when(modelRepository.findById(Fixtures.UNIT, modelId)).thenReturn(Optional.empty());

        CreateItemImpl useCase = new CreateItemImpl(itemRepository, modelRepository);
        CreateItemCommand command = command(modelId, Fixtures.UNIT);

        assertThrows(ModelNotFoundException.class, () -> useCase.execute(command));
        verify(itemRepository, never()).save(any(Item.class));
    }
}
