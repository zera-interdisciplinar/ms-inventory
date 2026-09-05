package com.zera.ms_inventory.core.usecase.model;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.repository.ModelRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindAllModelsImplTest {

    @Mock
    private ModelRepository modelRepository;

    @Test
    void shouldReturnOnlyTheGivenUnit() {
        Model model = Fixtures.model(Fixtures.UNIT);
        when(modelRepository.findAll(Fixtures.UNIT)).thenReturn(List.of(model));

        List<Model> result = new FindAllModelsImpl(modelRepository).execute(Fixtures.UNIT);

        assertEquals(1, result.size());
        assertEquals(Fixtures.UNIT, result.get(0).getUnitId());
    }

    @Test
    void shouldReturnEmptyForAnotherUnit() {
        when(modelRepository.findAll(Fixtures.OTHER_UNIT)).thenReturn(List.of());

        assertTrue(new FindAllModelsImpl(modelRepository).execute(Fixtures.OTHER_UNIT).isEmpty());
    }
}
