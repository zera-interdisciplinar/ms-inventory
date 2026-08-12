package com.zera.ms_inventory.core.usecase.model;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.exception.ModelNotFoundException;
import com.zera.ms_inventory.core.repository.ModelRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateModelExpectedLifespanMonthsImplTest {

    @Mock
    private ModelRepository modelRepository;

    @Test
    void shouldUpdateExpectedLifespanMonths() {
        UUID id = UUID.randomUUID();
        Model model = new Model(id, "Laptop X1", "Acme", 24, 60, Set.of("Lithium"));
        when(modelRepository.findById(id)).thenReturn(Optional.of(model));
        when(modelRepository.save(model)).thenReturn(model);

        UpdateModelExpectedLifespanMonthsImpl useCase = new UpdateModelExpectedLifespanMonthsImpl(modelRepository);
        Model result = useCase.execute(id, 72);

        assertEquals(72, result.getExpectedLifespanMonths());
        verify(modelRepository).save(model);
    }

    @Test
    void shouldThrowWhenModelDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(modelRepository.findById(id)).thenReturn(Optional.empty());

        UpdateModelExpectedLifespanMonthsImpl useCase = new UpdateModelExpectedLifespanMonthsImpl(modelRepository);

        assertThrows(ModelNotFoundException.class, () -> useCase.execute(id, 72));
    }
}
