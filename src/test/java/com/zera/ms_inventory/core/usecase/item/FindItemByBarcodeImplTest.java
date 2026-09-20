package com.zera.ms_inventory.core.usecase.item;

import java.util.Optional;

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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindItemByBarcodeImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Test
    void shouldFindTheItemByBarcodeWithinTheUnit() {
        Item item = Fixtures.item(Fixtures.UNIT);
        when(itemRepository.findByBarcode(Fixtures.UNIT, "7891234567890")).thenReturn(Optional.of(item));

        assertEquals(item, new FindItemByBarcodeImpl(itemRepository).execute(Fixtures.UNIT, "7891234567890"));
    }

    @Test
    void shouldThrowWithTheBarcodeWhenNotFound() {
        when(itemRepository.findByBarcode(Fixtures.UNIT, "111111-J")).thenReturn(Optional.empty());

        FindItemByBarcodeImpl useCase = new FindItemByBarcodeImpl(itemRepository);

        ItemNotFoundException ex = assertThrows(ItemNotFoundException.class,
                () -> useCase.execute(Fixtures.UNIT, "111111-J"));
        assertEquals("Item not found with barcode: 111111-J", ex.getMessage());
    }
}
