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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateCategoryDescriptionImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Test
    void shouldUpdateCategoryDescription() {
        UUID id = UUID.randomUUID();
        Category category = new Category(id, "Electronics", "Devices");
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        when(categoryRepository.save(category)).thenReturn(category);

        UpdateCategoryDescriptionImpl useCase = new UpdateCategoryDescriptionImpl(categoryRepository);
        Category result = useCase.execute(id, "Computer parts");

        assertEquals("Computer parts", result.getDescription());
        verify(categoryRepository).save(category);
    }

    @Test
    void shouldThrowWhenCategoryDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        UpdateCategoryDescriptionImpl useCase = new UpdateCategoryDescriptionImpl(categoryRepository);

        assertThrows(CategoryNotFoundException.class, () -> useCase.execute(id, "Computer parts"));
    }
}
