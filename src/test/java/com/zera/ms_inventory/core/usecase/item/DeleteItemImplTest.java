package com.zera.ms_inventory.core.usecase.item;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.repository.ItemRepository;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteItemImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Test
    void shouldDelete() {
        UUID id = UUID.randomUUID();
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(Fixtures.item(id, Fixtures.UNIT)));

        new DeleteItemImpl(itemRepository).execute(Fixtures.UNIT, id);

        verify(itemRepository).deleteById(Fixtures.UNIT, id);
    }

    @Test
    void shouldNotDeleteFromAnotherUnit() {
        UUID id = UUID.randomUUID();
        when(itemRepository.findById(Fixtures.OTHER_UNIT, id)).thenReturn(Optional.empty());

        DeleteItemImpl useCase = new DeleteItemImpl(itemRepository);

        assertThrows(ItemNotFoundException.class, () -> useCase.execute(Fixtures.OTHER_UNIT, id));
        verify(itemRepository, never()).deleteById(Fixtures.OTHER_UNIT, id);
    }
}
