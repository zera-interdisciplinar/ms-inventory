package com.zera.ms_inventory.core.usecase.model;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.exception.ModelNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
import com.zera.ms_inventory.core.repository.ItemRepository;
import com.zera.ms_inventory.core.repository.ModelRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListModelItemsImplTest {

    @Mock
    private ModelRepository modelRepository;

    @Mock
    private ItemRepository itemRepository;

    @Test
    void shouldListTheItemsOfTheModel() {
        UUID modelId = UUID.randomUUID();
        Pagination pagination = new Pagination(0, 20);
        PageResult<Item> page = new PageResult<>(List.of(Fixtures.item(Fixtures.UNIT)), 0, 20, 1);
        when(modelRepository.findById(Fixtures.UNIT, modelId)).thenReturn(Optional.of(Fixtures.model(modelId, Fixtures.UNIT)));
        when(itemRepository.findPageByModel(Fixtures.UNIT, modelId, pagination)).thenReturn(page);

        assertEquals(page, new ListModelItemsImpl(modelRepository, itemRepository).execute(Fixtures.UNIT, modelId, pagination));
    }

    @Test
    void shouldReturn404ForAModelOutsideTheUnit() {
        UUID modelId = UUID.randomUUID();
        when(modelRepository.findById(Fixtures.OTHER_UNIT, modelId)).thenReturn(Optional.empty());

        ListModelItemsImpl useCase = new ListModelItemsImpl(modelRepository, itemRepository);

        assertThrows(ModelNotFoundException.class,
                () -> useCase.execute(Fixtures.OTHER_UNIT, modelId, new Pagination(0, 20)));
        verify(itemRepository, never()).findPageByModel(any(), any(), any());
    }
}
