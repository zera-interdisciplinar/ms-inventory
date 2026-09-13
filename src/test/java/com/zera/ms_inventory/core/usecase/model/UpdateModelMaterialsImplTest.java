package com.zera.ms_inventory.core.usecase.model;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Material;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.exception.ModelNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.MaterialCode;
import com.zera.ms_inventory.core.repository.ModelRepository;
import com.zera.ms_inventory.core.usecase.material.MaterialResolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateModelMaterialsImplTest {

    @Mock
    private ModelRepository modelRepository;

    @Mock
    private MaterialResolver materialResolver;

    @Test
    void shouldReplaceTheMaterials() {
        UUID id = UUID.randomUUID();
        Model model = Fixtures.model(id, Fixtures.UNIT);
        Material metal = new Material(UUID.randomUUID(), MaterialCode.METAL, "Metal", true, false, "guia");
        when(modelRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(model));
        when(materialResolver.resolve(Set.of(MaterialCode.METAL))).thenReturn(Set.of(metal));
        when(modelRepository.save(model)).thenReturn(model);

        UpdateModelMaterialsImpl useCase = new UpdateModelMaterialsImpl(modelRepository, materialResolver);
        Model result = useCase.execute(Fixtures.UNIT, id, Set.of(MaterialCode.METAL));

        assertEquals(Set.of(metal), result.getMaterials());
        verify(modelRepository).save(model);
    }

    @Test
    void shouldThrowWhenNotFoundInThisUnit() {
        UUID id = UUID.randomUUID();
        when(modelRepository.findById(Fixtures.OTHER_UNIT, id)).thenReturn(Optional.empty());

        UpdateModelMaterialsImpl useCase = new UpdateModelMaterialsImpl(modelRepository, materialResolver);

        assertThrows(ModelNotFoundException.class,
                () -> useCase.execute(Fixtures.OTHER_UNIT, id, Set.of(MaterialCode.METAL)));
    }
}
