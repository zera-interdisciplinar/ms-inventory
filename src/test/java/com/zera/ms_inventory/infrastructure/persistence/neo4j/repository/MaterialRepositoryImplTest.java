package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.core.domain.entity.Material;
import com.zera.ms_inventory.core.domain.valueobject.MaterialCode;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.MaterialNode;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.MaterialMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaterialRepositoryImplTest {

    @Mock
    private MaterialNeo4jRepository neo4jRepository;

    private final MaterialMapper mapper = new MaterialMapper();

    private final MaterialNode plastic = new MaterialNode(UUID.randomUUID(), MaterialCode.PLASTIC, "Plástico",
            true, false, "Coleta seletiva de plásticos.");

    @Test
    void shouldListTheCatalogOrderedByName() {
        when(neo4jRepository.findAllByOrderByNameAsc()).thenReturn(List.of(plastic));

        List<Material> result = new MaterialRepositoryImpl(neo4jRepository, mapper).findAll();

        assertEquals(1, result.size());
        assertEquals(MaterialCode.PLASTIC, result.get(0).getCode());
        assertTrue(result.get(0).isRecyclable());
    }

    @Test
    void shouldFindByCode() {
        when(neo4jRepository.findByCode(MaterialCode.PLASTIC)).thenReturn(Optional.of(plastic));

        Optional<Material> result = new MaterialRepositoryImpl(neo4jRepository, mapper).findByCode(MaterialCode.PLASTIC);

        assertEquals("Plástico", result.orElseThrow().getName());
    }

    @Test
    void shouldMapBothWaysAndKeepNulls() {
        Material material = mapper.toDomain(plastic);

        assertEquals(plastic.getId(), mapper.toNode(material).getId());
        assertNull(mapper.toDomain(null));
        assertNull(mapper.toNode(null));
    }

    @Test
    void shouldFindAllByCodesAndSkipTheQueryWhenEmpty() {
        when(neo4jRepository.findAllByCodeIn(java.util.Set.of(MaterialCode.PLASTIC))).thenReturn(List.of(plastic));
        MaterialRepositoryImpl repository = new MaterialRepositoryImpl(neo4jRepository, mapper);

        assertEquals(1, repository.findAllByCodes(java.util.Set.of(MaterialCode.PLASTIC)).size());
        assertTrue(repository.findAllByCodes(java.util.Set.of()).isEmpty());
    }
}
