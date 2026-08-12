package com.zera.ms_inventory.core.usecase.category;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void shouldReturnAllCategories() {
        Category category = new Category(UUID.randomUUID(), "Electronics", "Devices");
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        FindAllCategoriesImpl useCase = new FindAllCategoriesImpl(categoryRepository);

        assertEquals(List.of(category), useCase.execute());
    }

    @Test
    void shouldReturnEmptyListWhenNoCategoriesExist() {
        when(categoryRepository.findAll()).thenReturn(List.of());

        FindAllCategoriesImpl useCase = new FindAllCategoriesImpl(categoryRepository);

        assertTrue(useCase.execute().isEmpty());
    }
}
