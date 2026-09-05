package com.zera.ms_inventory.core.usecase.model;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.exception.ModelNotFoundException;
import com.zera.ms_inventory.core.repository.ModelRepository;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteModelImplTest {

    @Mock
    private ModelRepository modelRepository;

    @Test
    void shouldDelete() {
        UUID id = UUID.randomUUID();
        when(modelRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(Fixtures.model(id, Fixtures.UNIT)));

        new DeleteModelImpl(modelRepository).execute(Fixtures.UNIT, id);

        verify(modelRepository).deleteById(Fixtures.UNIT, id);
    }

    @Test
    void shouldNotDeleteFromAnotherUnit() {
        UUID id = UUID.randomUUID();
        when(modelRepository.findById(Fixtures.OTHER_UNIT, id)).thenReturn(Optional.empty());

        DeleteModelImpl useCase = new DeleteModelImpl(modelRepository);

        assertThrows(ModelNotFoundException.class, () -> useCase.execute(Fixtures.OTHER_UNIT, id));
        verify(modelRepository, never()).deleteById(Fixtures.OTHER_UNIT, id);
    }
}
