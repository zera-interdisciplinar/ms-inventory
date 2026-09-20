package com.zera.ms_inventory.core.usecase.item;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
import com.zera.ms_inventory.core.repository.ItemRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListItemsImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Test
    void shouldReturnTheRequestedPageOfTheUnit() {
        Pagination pagination = new Pagination(0, 20);
        PageResult<Item> page = new PageResult<>(List.of(Fixtures.item(Fixtures.UNIT)), 0, 20, 1);
        when(itemRepository.findPage(Fixtures.UNIT, pagination)).thenReturn(page);

        PageResult<Item> result = new ListItemsImpl(itemRepository).execute(Fixtures.UNIT, pagination);

        assertEquals(page, result);
    }
}
