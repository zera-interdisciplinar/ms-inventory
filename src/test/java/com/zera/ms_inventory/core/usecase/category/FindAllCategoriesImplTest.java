package com.zera.ms_inventory.core.usecase.category;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Category;
import com.zera.ms_inventory.core.repository.CategoryRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindAllCategoriesImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Test
    void shouldReturnOnlyTheGivenUnit() {
        Category category = Fixtures.category(Fixtures.UNIT);
        when(categoryRepository.findAll(Fixtures.UNIT)).thenReturn(List.of(category));

        List<Category> result = new FindAllCategoriesImpl(categoryRepository).execute(Fixtures.UNIT);

        assertEquals(1, result.size());
        assertEquals(Fixtures.UNIT, result.get(0).getUnitId());
    }

    @Test
    void shouldReturnEmptyForAnotherUnit() {
        when(categoryRepository.findAll(Fixtures.OTHER_UNIT)).thenReturn(List.of());

        assertTrue(new FindAllCategoriesImpl(categoryRepository).execute(Fixtures.OTHER_UNIT).isEmpty());
    }
}
