package com.zera.ms_inventory.core.usecase.category;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.core.domain.entity.Category;
import com.zera.ms_inventory.core.domain.exception.CategoryNotFoundException;
import com.zera.ms_inventory.core.repository.CategoryRepository;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteCategoryImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Test
    void shouldDeleteCategoryWhenItExists() {
        UUID id = UUID.randomUUID();
        Category category = new Category(id, "Electronics", "Devices");
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

        DeleteCategoryImpl useCase = new DeleteCategoryImpl(categoryRepository);
        useCase.execute(id);

        verify(categoryRepository).deleteById(id);
    }

    @Test
    void shouldThrowWhenCategoryDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        DeleteCategoryImpl useCase = new DeleteCategoryImpl(categoryRepository);

        assertThrows(CategoryNotFoundException.class, () -> useCase.execute(id));
    }
}
