package com.zera.ms_inventory.core.usecase.material;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.core.domain.entity.Material;
import com.zera.ms_inventory.core.domain.exception.MaterialNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.MaterialCode;
import com.zera.ms_inventory.core.repository.MaterialRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaterialUseCasesTest {

    @Mock
    private MaterialRepository materialRepository;

    private final Material battery = new Material(UUID.randomUUID(), MaterialCode.BATTERY, "Pilhas e baterias",
            true, true, "Leve a pontos de coleta de pilhas e baterias.");

    @Test
    void shouldListTheCatalog() {
        when(materialRepository.findAll()).thenReturn(List.of(battery));

        assertEquals(List.of(battery), new ListMaterialsImpl(materialRepository).execute());
    }

    @Test
    void shouldFindMaterialByCode() {
        when(materialRepository.findByCode(MaterialCode.BATTERY)).thenReturn(Optional.of(battery));

        Material result = new FindMaterialByCodeImpl(materialRepository).execute(MaterialCode.BATTERY);

        assertTrue(result.isHazardous());
    }

    @Test
    void shouldThrowWhenMaterialIsNotInTheCatalog() {
        when(materialRepository.findByCode(MaterialCode.GLASS)).thenReturn(Optional.empty());

        FindMaterialByCodeImpl useCase = new FindMaterialByCodeImpl(materialRepository);

        assertThrows(MaterialNotFoundException.class, () -> useCase.execute(MaterialCode.GLASS));
    }
}
