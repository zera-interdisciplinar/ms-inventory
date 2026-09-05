package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.exception.ModelNotFoundException;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.ItemNode;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.CategoryMapper;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.ItemMapper;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.ModelMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRepositoryImplTest {

    @Mock
    private ItemNeo4jRepository neo4jRepository;

    @Mock
    private ModelNeo4jRepository modelNeo4jRepository;

    private final ModelMapper modelMapper = new ModelMapper(new CategoryMapper());
    private final ItemMapper mapper = new ItemMapper(modelMapper);

    private ItemRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new ItemRepositoryImpl(neo4jRepository, modelNeo4jRepository, mapper);
    }

    @Test
    void shouldAttachTheStoredModelOnSave() {
        Item item = Fixtures.item(Fixtures.UNIT);
        UUID modelId = item.getModel().getId();
        when(modelNeo4jRepository.findByIdAndUnitId(modelId, Fixtures.UNIT))
                .thenReturn(Optional.of(modelMapper.toNode(item.getModel())));
        when(neo4jRepository.save(any(ItemNode.class))).thenAnswer(i -> i.getArgument(0));

        Item result = repository.save(item);

        assertEquals(modelId, result.getModel().getId());
    }

    @Test
    void shouldRejectModelFromAnotherUnit() {
        Item item = Fixtures.item(Fixtures.UNIT);
        when(modelNeo4jRepository.findByIdAndUnitId(item.getModel().getId(), Fixtures.UNIT))
                .thenReturn(Optional.empty());

        assertThrows(ModelNotFoundException.class, () -> repository.save(item));
        verify(neo4jRepository, never()).save(any(ItemNode.class));
    }

    @Test
    void shouldSaveItemWithoutModel() {
        Item item = Fixtures.item(UUID.randomUUID(), Fixtures.UNIT, (Model) null);
        when(neo4jRepository.save(any(ItemNode.class))).thenAnswer(i -> i.getArgument(0));

        assertEquals(item.getId(), repository.save(item).getId());
        verify(modelNeo4jRepository, never()).findByIdAndUnitId(any(), any());
    }

    @Test
    void shouldFindByIdWithinTheUnit() {
        UUID id = UUID.randomUUID();
        when(neo4jRepository.findByIdAndUnitId(id, Fixtures.UNIT))
                .thenReturn(Optional.of(mapper.toNode(Fixtures.item(id, Fixtures.UNIT))));

        assertEquals(id, repository.findById(Fixtures.UNIT, id).orElseThrow().getId());
    }

    @Test
    void shouldReturnEmptyForAnotherUnit() {
        UUID id = UUID.randomUUID();
        when(neo4jRepository.findByIdAndUnitId(id, Fixtures.OTHER_UNIT)).thenReturn(Optional.empty());

        assertTrue(repository.findById(Fixtures.OTHER_UNIT, id).isEmpty());
    }

    @Test
    void shouldFindAllWithinTheUnit() {
        when(neo4jRepository.findAllByUnitId(Fixtures.UNIT))
                .thenReturn(List.of(mapper.toNode(Fixtures.item(Fixtures.UNIT))));

        assertEquals(1, repository.findAll(Fixtures.UNIT).size());
    }

    @Test
    void shouldFindByModelIdsWithinTheUnit() {
        UUID modelId = UUID.randomUUID();
        when(neo4jRepository.findAllByUnitIdAndModelIdIn(Fixtures.UNIT, List.of(modelId)))
                .thenReturn(List.of(mapper.toNode(Fixtures.item(Fixtures.UNIT))));

        assertEquals(1, repository.findAllByModelIds(Fixtures.UNIT, List.of(modelId)).size());
    }

    @Test
    void shouldSkipTheQueryWhenThereAreNoModelIds() {
        assertTrue(repository.findAllByModelIds(Fixtures.UNIT, List.of()).isEmpty());

        verify(neo4jRepository, never()).findAllByUnitIdAndModelIdIn(any(), any());
    }

    @Test
    void shouldDeleteWithinTheUnit() {
        UUID id = UUID.randomUUID();

        repository.deleteById(Fixtures.UNIT, id);

        verify(neo4jRepository).deleteByIdAndUnitId(id, Fixtures.UNIT);
    }
}
