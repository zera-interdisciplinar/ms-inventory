package com.zera.ms_inventory.core.usecase.item;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
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
    void shouldUpdateNextPredictionDate() {
        UUID id = UUID.randomUUID();
        Item item = new Item(id, new Barcode("123456"), ItemStatus.OK, UUID.randomUUID(), LocalDateTime.now(), 12, 5, "SN-001", LocalDate.now());
        LocalDateTime newPredictionDate = LocalDateTime.now().plusMonths(6);
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);

        UpdateItemNextPredictionDateImpl useCase = new UpdateItemNextPredictionDateImpl(itemRepository);
        Item result = useCase.execute(id, newPredictionDate);

        assertEquals(newPredictionDate, result.getNextPredictionDate());
        verify(itemRepository).save(item);
    }

    @Test
    void shouldThrowWhenItemDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(itemRepository.findById(id)).thenReturn(Optional.empty());

        UpdateItemNextPredictionDateImpl useCase = new UpdateItemNextPredictionDateImpl(itemRepository);

        assertThrows(ItemNotFoundException.class, () -> useCase.execute(id, LocalDateTime.now()));
    }
}
