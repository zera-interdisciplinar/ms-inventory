package com.zera.ms_inventory.core.usecase.item;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.repository.ItemRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindAllItemsImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Test
    void shouldReturnAllItems() {
        Item item = new Item(UUID.randomUUID(), new Barcode("123456"), ItemStatus.OK, UUID.randomUUID(), LocalDateTime.now(), 12, 5, "SN-001", LocalDate.now());
        when(itemRepository.findAll()).thenReturn(List.of(item));

        FindAllItemsImpl useCase = new FindAllItemsImpl(itemRepository);

        assertEquals(List.of(item), useCase.execute());
    }

    @Test
    void shouldReturnEmptyListWhenNoItemsExist() {
        when(itemRepository.findAll()).thenReturn(List.of());

        FindAllItemsImpl useCase = new FindAllItemsImpl(itemRepository);

        assertTrue(useCase.execute().isEmpty());
    }
}
