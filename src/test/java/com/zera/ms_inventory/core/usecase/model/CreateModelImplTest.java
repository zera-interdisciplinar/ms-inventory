package com.zera.ms_inventory.core.usecase.model;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.repository.ModelRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateModelImplTest {

    @Mock
    private ModelRepository modelRepository;

    @Test
    void shouldCreateModel() {
        when(modelRepository.save(any(Model.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateModelImpl useCase = new CreateModelImpl(modelRepository);
        Model result = useCase.execute("Laptop X1", "Acme", 24, 60, Set.of("Lithium"));

        assertEquals("Laptop X1", result.getName());
        assertEquals("Acme", result.getManufacturer());
    }
}
