package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.ItemFilter;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
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

    /** Mesma derivacao do repositorio: status que a maquina de estados deixa ir para DISPOSED. */
    private static final List<String> DISPOSABLE = java.util.Arrays.stream(ItemStatus.values())
            .filter(s -> s.canTransitionTo(ItemStatus.DISPOSED))
            .map(ItemStatus::name)
            .toList();

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

    @Test
    void shouldPageTheFilteredItemsWithinTheUnit() {
        UUID categoryId = UUID.randomUUID();
        ItemFilter filter = new ItemFilter(ItemStatus.IN_STOCK, categoryId, null, "placa");
        when(neo4jRepository.countFiltered(Fixtures.UNIT, "IN_STOCK", categoryId, null, "placa", false,
                DISPOSABLE, null, null)).thenReturn(11L);
        when(neo4jRepository.findFilteredPage(Fixtures.UNIT, "IN_STOCK", categoryId, null, "placa", false,
                DISPOSABLE, null, null, 10L, 10))
                .thenReturn(List.of(mapper.toNode(Fixtures.item(Fixtures.UNIT))));

        PageResult<Item> result = repository.findPage(Fixtures.UNIT, filter, new Pagination(1, 10));

        assertEquals(1, result.content().size());
        assertEquals(11, result.totalElements());
        assertEquals(2, result.totalPages());
    }

    @Test
    void shouldFilterOnlyWhatCanBeDisposed() {
        ItemFilter filter = new ItemFilter(null, null, null, null, true);
        when(neo4jRepository.countFiltered(Fixtures.UNIT, null, null, null, null, true, DISPOSABLE, null, null))
                .thenReturn(1L);
        when(neo4jRepository.findFilteredPage(Fixtures.UNIT, null, null, null, null, true, DISPOSABLE, null, null, 0L, 20))
                .thenReturn(List.of(mapper.toNode(Fixtures.item(Fixtures.UNIT))));

        PageResult<Item> result = repository.findPage(Fixtures.UNIT, filter, new Pagination(0, 20));

        assertEquals(1, result.content().size());
        // o filtro nasce da maquina de estados, entao acompanha qualquer estado novo
        assertTrue(DISPOSABLE.contains(ItemStatus.IN_STOCK.name()));
        assertTrue(DISPOSABLE.contains(ItemStatus.AWAITING_EVALUATION.name()));
        assertTrue(!DISPOSABLE.contains(ItemStatus.DRAFT.name()));
    }

    @Test
    void shouldSkipThePageQueryWhenNothingMatches() {
        when(neo4jRepository.countFiltered(Fixtures.UNIT, null, null, null, null, false, DISPOSABLE, null, null))
                .thenReturn(0L);

        PageResult<Item> result = repository.findPage(Fixtures.UNIT, ItemFilter.none(), new Pagination(0, 20));

        assertTrue(result.content().isEmpty());
        verify(neo4jRepository, never()).findFilteredPage(any(), any(), any(), any(), any(),
                org.mockito.ArgumentMatchers.anyBoolean(), any(), any(), any(),
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void shouldPageTheItemsOfAModelWithinTheUnit() {
        UUID modelId = UUID.randomUUID();
        PageRequest request = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        when(neo4jRepository.findAllByUnitIdAndModelId(Fixtures.UNIT, modelId, request))
                .thenReturn(new PageImpl<>(List.of(mapper.toNode(Fixtures.item(Fixtures.UNIT))), request, 6));

        PageResult<Item> result = repository.findPageByModel(Fixtures.UNIT, modelId, new Pagination(0, 5));

        assertEquals(1, result.content().size());
        assertEquals(2, result.totalPages());
    }

    @Test
    void shouldCheckWhetherTheModelHasItemsInTheUnit() {
        UUID modelId = UUID.randomUUID();
        when(neo4jRepository.existsByUnitIdAndModelId(Fixtures.UNIT, modelId)).thenReturn(true);

        assertTrue(repository.existsByModel(Fixtures.UNIT, modelId));
    }

    @Test
    void shouldFindByBarcodeAndCheckDisplayCodesWithinTheUnit() {
        Item item = Fixtures.item(Fixtures.UNIT);
        item.assignDisplayCode("265964");
        when(neo4jRepository.findByUnitIdAndBarcode(Fixtures.UNIT, "7891234567890"))
                .thenReturn(Optional.of(mapper.toNode(item)));
        when(neo4jRepository.existsByUnitIdAndDisplayCode(Fixtures.UNIT, "265964")).thenReturn(true);

        assertEquals("265964", repository.findByBarcode(Fixtures.UNIT, "7891234567890").orElseThrow().getDisplayCode());
        assertTrue(repository.existsByDisplayCode(Fixtures.UNIT, "265964"));
    }

    @Test
    void shouldTellWhetherAnIdIsAlreadyTakenInAnyUnit() {
        UUID id = UUID.randomUUID();
        when(neo4jRepository.existsById(id)).thenReturn(true);

        assertTrue(repository.existsAnyWithId(id));
    }
}
