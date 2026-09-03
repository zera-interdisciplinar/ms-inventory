package com.zera.ms_inventory.core.usecase.category;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
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
    void shouldCreateCategoryStampedWithTheGivenUnit() {
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));
        LocalDateTime now = LocalDateTime.of(2026, 8, 4, 10, 0);

        CreateCategoryImpl useCase = new CreateCategoryImpl(categoryRepository);
        Category result = useCase.execute(Fixtures.UNIT, "Electronics", "Devices", now, now);

        assertNotNull(result.getId());
        assertEquals(Fixtures.UNIT, result.getUnitId());
        assertEquals("Electronics", result.getName());
        assertEquals("Devices", result.getDescription());
        assertEquals(now, result.getCreatedAt());
        verify(categoryRepository).save(result);
    }
}
