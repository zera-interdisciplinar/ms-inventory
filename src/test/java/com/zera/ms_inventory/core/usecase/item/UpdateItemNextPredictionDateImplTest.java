package com.zera.ms_inventory.core.usecase.item;

import java.time.LocalDateTime;
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
class UpdateItemNextPredictionDateImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Test
    void shouldUpdate() {
        UUID id = UUID.randomUUID();
        Item item = Fixtures.item(id, Fixtures.UNIT);
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);

        UpdateItemNextPredictionDateImpl useCase = new UpdateItemNextPredictionDateImpl(itemRepository);
        Item result = useCase.execute(Fixtures.UNIT, id, LocalDateTime.of(2026, 9, 1, 8, 0));

        assertEquals(LocalDateTime.of(2026, 9, 1, 8, 0), result.getNextPredictionDate());
        verify(itemRepository).save(item);
    }

    @Test
    void shouldThrowWhenNotFoundInThisUnit() {
        UUID id = UUID.randomUUID();
        when(itemRepository.findById(Fixtures.OTHER_UNIT, id)).thenReturn(Optional.empty());

        UpdateItemNextPredictionDateImpl useCase = new UpdateItemNextPredictionDateImpl(itemRepository);

        assertThrows(ItemNotFoundException.class, () -> useCase.execute(Fixtures.OTHER_UNIT, id, LocalDateTime.of(2026, 9, 1, 8, 0)));
    }
}
