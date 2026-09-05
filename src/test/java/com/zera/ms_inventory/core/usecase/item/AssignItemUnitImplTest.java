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
class AssignItemUnitImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Test
    void shouldMoveItemToAnotherUnit() {
        UUID id = UUID.randomUUID();
        Item item = Fixtures.item(id, Fixtures.UNIT);
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);

        AssignItemUnitImpl useCase = new AssignItemUnitImpl(itemRepository);
        Item result = useCase.execute(Fixtures.UNIT, id, Fixtures.OTHER_UNIT);

        assertEquals(Fixtures.OTHER_UNIT, result.getUnitId());
        verify(itemRepository).save(item);
    }

    @Test
    void shouldNotMoveAnItemOfAnotherUnit() {
        UUID id = UUID.randomUUID();
        when(itemRepository.findById(Fixtures.OTHER_UNIT, id)).thenReturn(Optional.empty());

        AssignItemUnitImpl useCase = new AssignItemUnitImpl(itemRepository);

        assertThrows(ItemNotFoundException.class, () -> useCase.execute(Fixtures.OTHER_UNIT, id, Fixtures.UNIT));
    }
}
