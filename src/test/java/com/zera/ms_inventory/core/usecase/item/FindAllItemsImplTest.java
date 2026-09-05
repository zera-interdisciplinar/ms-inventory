package com.zera.ms_inventory.core.usecase.item;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.repository.ItemRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindAllItemsImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Test
    void shouldReturnOnlyTheGivenUnit() {
        Item item = Fixtures.item(Fixtures.UNIT);
        when(itemRepository.findAll(Fixtures.UNIT)).thenReturn(List.of(item));

        List<Item> result = new FindAllItemsImpl(itemRepository).execute(Fixtures.UNIT);

        assertEquals(1, result.size());
        assertEquals(Fixtures.UNIT, result.get(0).getUnitId());
    }

    @Test
    void shouldReturnEmptyForAnotherUnit() {
        when(itemRepository.findAll(Fixtures.OTHER_UNIT)).thenReturn(List.of());

        assertTrue(new FindAllItemsImpl(itemRepository).execute(Fixtures.OTHER_UNIT).isEmpty());
    }
}
