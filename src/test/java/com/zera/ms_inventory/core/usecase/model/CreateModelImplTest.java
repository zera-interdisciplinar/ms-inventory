package com.zera.ms_inventory.core.usecase.model;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.repository.ModelRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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

        assertNotNull(result.getId());
        assertEquals("Laptop X1", result.getName());
        assertEquals("Acme", result.getManufacturer());
        verify(modelRepository).save(result);
    }

    @Test
    void shouldNotBeAffectedByMutatingCallerSetAfterCreation() {
        when(modelRepository.save(any(Model.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Set<String> callerMaterials = new HashSet<>(Set.of("Lithium"));

        CreateModelImpl useCase = new CreateModelImpl(modelRepository);
        Model result = useCase.execute("Laptop X1", "Acme", 24, 60, callerMaterials);
        callerMaterials.add("Mercury");

        assertEquals(Set.of("Lithium"), result.getHazardousMaterials());
    }
}
