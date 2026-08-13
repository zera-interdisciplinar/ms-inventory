package com.zera.ms_inventory.core.usecase.item;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateItemImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Test
    void shouldCreateItem() {
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateItemImpl useCase = new CreateItemImpl(itemRepository);
        CreateItemCommand command = new CreateItemCommand(new Barcode("123456"), ItemStatus.OK, UUID.randomUUID(),
                LocalDateTime.now(), 12, 5, "SN-001", LocalDate.now());
        Item result = useCase.execute(command);

        assertNotNull(result.getId());
        assertEquals(ItemStatus.OK, result.getStatus());
        assertEquals("SN-001", result.getSerialNumber());
        verify(itemRepository).save(result);
    }
}
