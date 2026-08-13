package com.zera.ms_inventory.core.usecase.model;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void shouldReturnModelWhenItExists() {
        UUID id = UUID.randomUUID();
        Model model = new Model(id, "Laptop X1", "Acme", 24, 60, Set.of("Lithium"));
        when(modelRepository.findById(id)).thenReturn(Optional.of(model));

        FindModelByIdImpl useCase = new FindModelByIdImpl(modelRepository);

        assertEquals(model, useCase.execute(id));
    }

    @Test
    void shouldThrowWhenModelDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(modelRepository.findById(id)).thenReturn(Optional.empty());

        FindModelByIdImpl useCase = new FindModelByIdImpl(modelRepository);

        assertThrows(ModelNotFoundException.class, () -> useCase.execute(id));
    }
}
