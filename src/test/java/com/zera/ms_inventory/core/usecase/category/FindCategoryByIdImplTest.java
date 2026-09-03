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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindCategoryByIdImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Test
    void shouldFindById() {
        UUID id = UUID.randomUUID();
        Category category = Fixtures.category(id, Fixtures.UNIT);
        when(categoryRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(category));

        Category result = new FindCategoryByIdImpl(categoryRepository).execute(Fixtures.UNIT, id);

        assertEquals(id, result.getId());
    }

    @Test
    void shouldThrowWhenTheIdBelongsToAnotherUnit() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(Fixtures.OTHER_UNIT, id)).thenReturn(Optional.empty());

        FindCategoryByIdImpl useCase = new FindCategoryByIdImpl(categoryRepository);

        assertThrows(CategoryNotFoundException.class, () -> useCase.execute(Fixtures.OTHER_UNIT, id));
    }
}
