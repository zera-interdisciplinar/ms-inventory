package com.zera.ms_inventory.core.usecase.model;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void shouldReturnAllModels() {
        Model model = new Model(UUID.randomUUID(), "Laptop X1", "Acme", 24, 60, Set.of("Lithium"));
        when(modelRepository.findAll()).thenReturn(List.of(model));

        FindAllModelsImpl useCase = new FindAllModelsImpl(modelRepository);

        assertEquals(List.of(model), useCase.execute());
    }

    @Test
    void shouldReturnEmptyListWhenNoModelsExist() {
        when(modelRepository.findAll()).thenReturn(List.of());

        FindAllModelsImpl useCase = new FindAllModelsImpl(modelRepository);

        assertTrue(useCase.execute().isEmpty());
    }
}
