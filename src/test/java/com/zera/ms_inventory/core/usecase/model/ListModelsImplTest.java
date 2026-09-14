package com.zera.ms_inventory.core.usecase.model;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
import com.zera.ms_inventory.core.repository.ModelRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListModelsImplTest {

    @Mock
    private ModelRepository modelRepository;

    @Test
    void shouldReturnTheRequestedPageOfTheUnit() {
        Pagination pagination = new Pagination(0, 20);
        PageResult<Model> page = new PageResult<>(List.of(Fixtures.model(Fixtures.UNIT)), 0, 20, 1);
        when(modelRepository.findPage(Fixtures.UNIT, pagination)).thenReturn(page);

        PageResult<Model> result = new ListModelsImpl(modelRepository).execute(Fixtures.UNIT, pagination);

        assertEquals(page, result);
    }
}
