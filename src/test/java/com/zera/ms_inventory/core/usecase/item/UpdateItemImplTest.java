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
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.DamageType;
import com.zera.ms_inventory.core.domain.valueobject.ItemCondition;
import com.zera.ms_inventory.core.domain.valueobject.UsageIntensity;
import com.zera.ms_inventory.core.repository.ItemRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateItemImplTest {

    @Mock
    private ItemRepository itemRepository;

    private Item storedItem(UUID id) {
        Item item = Fixtures.item(id, Fixtures.UNIT);
        item.describe("Placa de vídeo", ItemCondition.SEMI_DAMAGED, true, Set.of(DamageType.OXIDATION), "Pino torto");
        return item;
    }

    private UpdateItemCommand command(UUID id, String name, ItemCondition condition, Boolean hasDamages,
                                      Set<DamageType> damages, String notes, String serialNumber) {
        return new UpdateItemCommand(Fixtures.UNIT, id, name, condition, hasDamages, damages, notes, serialNumber,
                null, null, null);
    }

    @Test
    void shouldChangeOnlyTheFieldsThatWereSent() {
        UUID id = UUID.randomUUID();
        Item item = storedItem(id);
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);

        Item result = new UpdateItemImpl(itemRepository)
                .execute(command(id, null, ItemCondition.DAMAGED, null, null, null, null));

        assertEquals(ItemCondition.DAMAGED, result.getCondition());
        assertEquals("Placa de vídeo", result.getName());
        assertEquals(Set.of(DamageType.OXIDATION), result.getDamages());
        assertEquals("Pino torto", result.getNotes());
        assertEquals("SN-001", result.getSerialNumber());
        verify(itemRepository).save(item);
    }

    @Test
    void shouldClearDamagesWhenTheItemNoLongerHasDamages() {
        UUID id = UUID.randomUUID();
        Item item = storedItem(id);
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);

        Item result = new UpdateItemImpl(itemRepository)
                .execute(command(id, null, ItemCondition.USED, false, null, null, null));

        assertEquals(Boolean.FALSE, result.getHasDamages());
        assertEquals(Set.of(), result.getDamages());
    }

    @Test
    void shouldClearOptionalTextWithBlankValuesAndUpdateLegacyFields() {
        UUID id = UUID.randomUUID();
        Item item = storedItem(id);
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);

        Item result = new UpdateItemImpl(itemRepository).execute(new UpdateItemCommand(Fixtures.UNIT, id,
                "Placa de vídeo RTX", null, null, Set.of(DamageType.OXIDATION, DamageType.MISSING_PART), "", " ",
                LocalDate.of(2025, 1, 10), 2023, UsageIntensity.LOW));

        assertEquals("Placa de vídeo RTX", result.getName());
        assertEquals(Set.of(DamageType.OXIDATION, DamageType.MISSING_PART), result.getDamages());
        assertNull(result.getNotes());
        assertNull(result.getSerialNumber());
        assertEquals(LocalDate.of(2025, 1, 10), result.getAcquiredAt());
        assertEquals(2023, result.getManufacturingYear());
        assertEquals(UsageIntensity.LOW, result.getUsageIntensity());
    }

    @Test
    void shouldRejectDamagesWhenMarkedAsWithoutDamages() {
        UUID id = UUID.randomUUID();
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(storedItem(id)));

        UpdateItemImpl useCase = new UpdateItemImpl(itemRepository);
        UpdateItemCommand command = command(id, null, null, false, Set.of(DamageType.OTHER), null, null);

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void shouldThrowWhenTheItemIsNotInTheUnit() {
        UUID id = UUID.randomUUID();
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.empty());

        UpdateItemImpl useCase = new UpdateItemImpl(itemRepository);
        UpdateItemCommand command = command(id, "x", null, null, null, null, null);

        assertThrows(ItemNotFoundException.class, () -> useCase.execute(command));
    }
}
