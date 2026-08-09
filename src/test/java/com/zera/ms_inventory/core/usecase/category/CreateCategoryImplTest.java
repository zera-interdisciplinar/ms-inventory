package com.zera.ms_inventory.core.usecase.category;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.core.domain.entity.Category;
import com.zera.ms_inventory.core.repository.CategoryRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCategoryImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Test
    void shouldCreateCategory() {
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateCategoryImpl useCase = new CreateCategoryImpl(categoryRepository);
        LocalDateTime now = LocalDateTime.now();
        Category result = useCase.execute("Electronics", "Devices", now, now);

        assertNotNull(result.getId());
        assertEquals("Electronics", result.getName());
        assertEquals("Devices", result.getDescription());
        verify(categoryRepository).save(result);
    }
}
