package com.zera.ms_inventory.core.usecase.category;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.exception.CategoryInUseException;
import com.zera.ms_inventory.core.domain.exception.CategoryNotFoundException;
import com.zera.ms_inventory.core.repository.CategoryRepository;
import com.zera.ms_inventory.core.repository.ModelRepository;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteCategoryImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ModelRepository modelRepository;

    @Test
    void shouldDelete() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(Fixtures.category(id, Fixtures.UNIT)));

        new DeleteCategoryImpl(categoryRepository, modelRepository).execute(Fixtures.UNIT, id);

        verify(categoryRepository).deleteById(Fixtures.UNIT, id);
    }

    @Test
    void shouldNotDeleteFromAnotherUnit() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(Fixtures.OTHER_UNIT, id)).thenReturn(Optional.empty());

        DeleteCategoryImpl useCase = new DeleteCategoryImpl(categoryRepository, modelRepository);

        assertThrows(CategoryNotFoundException.class, () -> useCase.execute(Fixtures.OTHER_UNIT, id));
        verify(categoryRepository, never()).deleteById(Fixtures.OTHER_UNIT, id);
    }

    @Test
    void shouldRefuseToDeleteACategoryThatStillHasModels() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(Fixtures.category(id, Fixtures.UNIT)));
        when(modelRepository.existsByCategory(Fixtures.UNIT, id)).thenReturn(true);

        DeleteCategoryImpl useCase = new DeleteCategoryImpl(categoryRepository, modelRepository);

        assertThrows(CategoryInUseException.class, () -> useCase.execute(Fixtures.UNIT, id));
        verify(categoryRepository, never()).deleteById(Fixtures.UNIT, id);
    }
}
