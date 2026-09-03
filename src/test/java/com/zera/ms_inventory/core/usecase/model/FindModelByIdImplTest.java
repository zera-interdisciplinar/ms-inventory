package com.zera.ms_inventory.core.usecase.model;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.exception.ModelNotFoundException;
import com.zera.ms_inventory.core.repository.ModelRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindModelByIdImplTest {

    @Mock
    private ModelRepository modelRepository;

    @Test
    void shouldFindById() {
        UUID id = UUID.randomUUID();
        Model model = Fixtures.model(id, Fixtures.UNIT);
        when(modelRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(model));

        Model result = new FindModelByIdImpl(modelRepository).execute(Fixtures.UNIT, id);

        assertEquals(id, result.getId());
    }

    @Test
    void shouldThrowWhenTheIdBelongsToAnotherUnit() {
        UUID id = UUID.randomUUID();
        when(modelRepository.findById(Fixtures.OTHER_UNIT, id)).thenReturn(Optional.empty());

        FindModelByIdImpl useCase = new FindModelByIdImpl(modelRepository);

        assertThrows(ModelNotFoundException.class, () -> useCase.execute(Fixtures.OTHER_UNIT, id));
    }
}
