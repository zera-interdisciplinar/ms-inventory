package com.zera.ms_inventory.core.usecase.model;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.exception.CategoryNotFoundException;
import com.zera.ms_inventory.core.repository.CategoryRepository;
import com.zera.ms_inventory.core.repository.ModelRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateModelImplTest {

    @Mock
    private ModelRepository modelRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Test
    void shouldCreateModelStampedWithTheGivenUnit() {
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findById(Fixtures.UNIT, categoryId))
                .thenReturn(Optional.of(Fixtures.category(categoryId, Fixtures.UNIT)));
        when(modelRepository.save(any(Model.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateModelImpl useCase = new CreateModelImpl(modelRepository, categoryRepository);
        Model result = useCase.execute(Fixtures.UNIT, "Laptop X1", "Acme", 24, 60, Set.of("Lithium"), categoryId);

        assertNotNull(result.getId());
        assertEquals(Fixtures.UNIT, result.getUnitId());
        assertEquals("Laptop X1", result.getName());
        assertEquals("Acme", result.getManufacturer());
        assertEquals(categoryId, result.getCategory().getId());
        verify(modelRepository).save(result);
    }

    @Test
    void shouldRejectCategoryFromAnotherUnit() {
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findById(Fixtures.UNIT, categoryId)).thenReturn(Optional.empty());

        CreateModelImpl useCase = new CreateModelImpl(modelRepository, categoryRepository);

        assertThrows(CategoryNotFoundException.class,
                () -> useCase.execute(Fixtures.UNIT, "Laptop X1", "Acme", 24, 60, Set.of("Lithium"), categoryId));
        verify(modelRepository, never()).save(any(Model.class));
    }

    @Test
    void shouldNotBeAffectedByMutatingCallerSetAfterCreation() {
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findById(Fixtures.UNIT, categoryId))
                .thenReturn(Optional.of(Fixtures.category(categoryId, Fixtures.UNIT)));
        when(modelRepository.save(any(Model.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Set<String> callerMaterials = new HashSet<>(Set.of("Lithium"));

        CreateModelImpl useCase = new CreateModelImpl(modelRepository, categoryRepository);
        Model result = useCase.execute(Fixtures.UNIT, "Laptop X1", "Acme", 24, 60, callerMaterials, categoryId);
        callerMaterials.add("Mercury");

        assertEquals(Set.of("Lithium"), result.getHazardousMaterials());
    }
}
