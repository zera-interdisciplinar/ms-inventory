package com.zera.ms_inventory.core.usecase.category;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
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
    void shouldUpdate() {
        UUID id = UUID.randomUUID();
        Category category = Fixtures.category(id, Fixtures.UNIT);
        when(categoryRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(category));
        when(categoryRepository.save(category)).thenReturn(category);

        UpdateCategoryDescriptionImpl useCase = new UpdateCategoryDescriptionImpl(categoryRepository);
        Category result = useCase.execute(Fixtures.UNIT, id, "Computer parts");

        assertEquals("Computer parts", result.getDescription());
        verify(categoryRepository).save(category);
    }

    @Test
    void shouldThrowWhenNotFoundInThisUnit() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(Fixtures.OTHER_UNIT, id)).thenReturn(Optional.empty());

        UpdateCategoryDescriptionImpl useCase = new UpdateCategoryDescriptionImpl(categoryRepository);

        assertThrows(CategoryNotFoundException.class, () -> useCase.execute(Fixtures.OTHER_UNIT, id, "Computer parts"));
    }
}
