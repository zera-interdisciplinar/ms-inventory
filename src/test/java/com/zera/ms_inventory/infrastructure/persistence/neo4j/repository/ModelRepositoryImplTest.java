package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.exception.CategoryNotFoundException;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.ModelNode;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.CategoryMapper;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.ModelMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModelRepositoryImplTest {

    private static final float[] VECTOR = {0.1f, 0.2f, 0.3f};

    @Mock
    private ModelNeo4jRepository neo4jRepository;

    @Mock
    private CategoryNeo4jRepository categoryNeo4jRepository;

    @Mock
    private EmbeddingModel embeddingModel;

    private final CategoryMapper categoryMapper = new CategoryMapper();
    private final ModelMapper mapper = new ModelMapper(categoryMapper);

    private ModelRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new ModelRepositoryImpl(neo4jRepository, categoryNeo4jRepository, mapper, embeddingModel);
    }

    @Test
    void shouldAttachTheStoredCategoryAndEmbedOnFirstSave() {
        Model model = Fixtures.model(Fixtures.UNIT);
        UUID categoryId = model.getCategory().getId();
        when(categoryNeo4jRepository.findByIdAndUnitId(categoryId, Fixtures.UNIT))
                .thenReturn(Optional.of(categoryMapper.toNode(model.getCategory())));
        when(neo4jRepository.findByIdAndUnitId(model.getId(), Fixtures.UNIT)).thenReturn(Optional.empty());
        when(embeddingModel.embed(model.toEmbeddableText())).thenReturn(VECTOR);
        when(neo4jRepository.save(any(ModelNode.class))).thenAnswer(i -> i.getArgument(0));

        Model result = repository.save(model);

        assertEquals(categoryId, result.getCategory().getId());
        verify(neo4jRepository).save(org.mockito.ArgumentMatchers.argThat(node -> {
            assertEquals(model.toEmbeddableText(), node.getEmbeddedText());
            assertEquals(categoryId, node.getCategory().getId());
            return true;
        }));
    }

    @Test
    void shouldRejectCategoryFromAnotherUnit() {
        Model model = Fixtures.model(Fixtures.UNIT);
        when(categoryNeo4jRepository.findByIdAndUnitId(model.getCategory().getId(), Fixtures.UNIT))
                .thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> repository.save(model));
        verify(neo4jRepository, never()).save(any(ModelNode.class));
    }

    @Test
    void shouldReuseTheStoredVectorWhenTheTextDidNotChange() {
        Model model = Fixtures.model(Fixtures.UNIT);
        ModelNode stored = mapper.toNode(model);
        stored.setEmbedding(VECTOR);
        stored.setEmbeddedText(model.toEmbeddableText());
        when(categoryNeo4jRepository.findByIdAndUnitId(model.getCategory().getId(), Fixtures.UNIT))
                .thenReturn(Optional.of(categoryMapper.toNode(model.getCategory())));
        when(neo4jRepository.findByIdAndUnitId(model.getId(), Fixtures.UNIT)).thenReturn(Optional.of(stored));
        when(neo4jRepository.save(any(ModelNode.class))).thenAnswer(i -> i.getArgument(0));

        repository.save(model);

        verify(embeddingModel, never()).embed(anyString());
    }

    @Test
    void shouldSaveModelWithoutCategory() {
        Model model = new Model(UUID.randomUUID(), Fixtures.UNIT, "Laptop", "Acme", 24, 60, java.util.Set.of(), null);
        when(neo4jRepository.findByIdAndUnitId(model.getId(), Fixtures.UNIT)).thenReturn(Optional.empty());
        when(embeddingModel.embed(model.toEmbeddableText())).thenReturn(VECTOR);
        when(neo4jRepository.save(any(ModelNode.class))).thenAnswer(i -> i.getArgument(0));

        assertEquals(model.getId(), repository.save(model).getId());
        verify(categoryNeo4jRepository, never()).findByIdAndUnitId(any(), any());
    }

    @Test
    void shouldFindByIdWithinTheUnit() {
        UUID id = UUID.randomUUID();
        when(neo4jRepository.findByIdAndUnitId(id, Fixtures.UNIT))
                .thenReturn(Optional.of(mapper.toNode(Fixtures.model(id, Fixtures.UNIT))));

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
                .thenReturn(List.of(mapper.toNode(Fixtures.model(Fixtures.UNIT))));

        assertEquals(1, repository.findAll(Fixtures.UNIT).size());
    }

    @Test
    void shouldOverFetchTenTimesTheLimitAndScopeTheSearchToTheUnit() {
        when(embeddingModel.embed("bateria de litio")).thenReturn(VECTOR);
        when(neo4jRepository.semanticSearch(any(), eq(Fixtures.UNIT), anyInt(), anyInt()))
                .thenReturn(List.of(mapper.toNode(Fixtures.model(Fixtures.UNIT))));

        List<Model> result = repository.semanticSearch(Fixtures.UNIT, "bateria de litio", 5);

        assertEquals(1, result.size());
        verify(neo4jRepository).semanticSearch(List.of(0.1f, 0.2f, 0.3f), Fixtures.UNIT, 50, 5);
    }

    @Test
    void shouldDeleteWithinTheUnit() {
        UUID id = UUID.randomUUID();

        repository.deleteById(Fixtures.UNIT, id);

        verify(neo4jRepository).deleteByIdAndUnitId(id, Fixtures.UNIT);
    }
}
