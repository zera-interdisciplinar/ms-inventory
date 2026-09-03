package com.zera.ms_inventory.core.usecase.model;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.exception.ModelNotFoundException;
import com.zera.ms_inventory.core.repository.ModelRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateModelWarrantyMonthsImplTest {

    @Mock
    private ModelRepository modelRepository;

    @Test
    void shouldUpdate() {
        UUID id = UUID.randomUUID();
        Model model = Fixtures.model(id, Fixtures.UNIT);
        when(modelRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(model));
        when(modelRepository.save(model)).thenReturn(model);

        UpdateModelWarrantyMonthsImpl useCase = new UpdateModelWarrantyMonthsImpl(modelRepository);
        Model result = useCase.execute(Fixtures.UNIT, id, 36);

        assertEquals(36, result.getWarrantyMonths());
        verify(modelRepository).save(model);
    }

    @Test
    void shouldThrowWhenNotFoundInThisUnit() {
        UUID id = UUID.randomUUID();
        when(modelRepository.findById(Fixtures.OTHER_UNIT, id)).thenReturn(Optional.empty());

        UpdateModelWarrantyMonthsImpl useCase = new UpdateModelWarrantyMonthsImpl(modelRepository);

        assertThrows(ModelNotFoundException.class, () -> useCase.execute(Fixtures.OTHER_UNIT, id, 36));
    }
}
