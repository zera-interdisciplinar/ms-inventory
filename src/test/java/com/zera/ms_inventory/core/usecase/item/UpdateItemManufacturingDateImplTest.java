package com.zera.ms_inventory.core.usecase.item;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.repository.ItemRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateItemManufacturingDateImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Test
    void shouldUpdate() {
        UUID id = UUID.randomUUID();
        Item item = Fixtures.item(id, Fixtures.UNIT);
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);

        UpdateItemManufacturingDateImpl useCase = new UpdateItemManufacturingDateImpl(itemRepository);
        Item result = useCase.execute(Fixtures.UNIT, id, 2025);

        assertEquals(2025, result.getManufacturingDate());
        verify(itemRepository).save(item);
    }

    @Test
    void shouldThrowWhenNotFoundInThisUnit() {
        UUID id = UUID.randomUUID();
        when(itemRepository.findById(Fixtures.OTHER_UNIT, id)).thenReturn(Optional.empty());

        UpdateItemManufacturingDateImpl useCase = new UpdateItemManufacturingDateImpl(itemRepository);

        assertThrows(ItemNotFoundException.class, () -> useCase.execute(Fixtures.OTHER_UNIT, id, 2025));
    }
}
