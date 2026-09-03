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
class UpdateItemSerialNumberImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Test
    void shouldUpdate() {
        UUID id = UUID.randomUUID();
        Item item = Fixtures.item(id, Fixtures.UNIT);
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);

        UpdateItemSerialNumberImpl useCase = new UpdateItemSerialNumberImpl(itemRepository);
        Item result = useCase.execute(Fixtures.UNIT, id, "SN-002");

        assertEquals("SN-002", result.getSerialNumber());
        verify(itemRepository).save(item);
    }

    @Test
    void shouldThrowWhenNotFoundInThisUnit() {
        UUID id = UUID.randomUUID();
        when(itemRepository.findById(Fixtures.OTHER_UNIT, id)).thenReturn(Optional.empty());

        UpdateItemSerialNumberImpl useCase = new UpdateItemSerialNumberImpl(itemRepository);

        assertThrows(ItemNotFoundException.class, () -> useCase.execute(Fixtures.OTHER_UNIT, id, "SN-002"));
    }
}
