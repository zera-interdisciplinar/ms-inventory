package com.zera.ms_inventory.core.usecase.material;

import java.util.List;
import java.util.Set;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaterialResolverTest {

    @Mock
    private MaterialRepository materialRepository;

    private final Material metal = new Material(UUID.randomUUID(), MaterialCode.METAL, "Metal", true, false, "guia");

    @Test
    void shouldResolveAllRequestedCodes() {
        when(materialRepository.findAllByCodes(Set.of(MaterialCode.METAL))).thenReturn(List.of(metal));

        assertEquals(Set.of(metal), new MaterialResolver(materialRepository).resolve(Set.of(MaterialCode.METAL)));
    }

    @Test
    void shouldFailWhenACodeIsMissingFromTheCatalog() {
        Set<MaterialCode> codes = Set.of(MaterialCode.METAL, MaterialCode.GLASS);
        when(materialRepository.findAllByCodes(codes)).thenReturn(List.of(metal));

        MaterialResolver resolver = new MaterialResolver(materialRepository);

        MaterialNotFoundException ex = assertThrows(MaterialNotFoundException.class, () -> resolver.resolve(codes));
        assertEquals("Material not found with code: GLASS", ex.getMessage());
    }
}
