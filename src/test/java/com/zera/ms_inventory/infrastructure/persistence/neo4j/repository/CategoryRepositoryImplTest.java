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
import com.zera.ms_inventory.core.domain.entity.Category;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.CategoryNode;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.CategoryMapper;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryRepositoryImplTest {

    private static final float[] VECTOR = {0.1f, 0.2f, 0.3f};

    @Mock
    private CategoryNeo4jRepository neo4jRepository;

    @Mock
    private EmbeddingModel embeddingModel;

    private final CategoryMapper mapper = new CategoryMapper();

    private CategoryRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new CategoryRepositoryImpl(neo4jRepository, mapper, embeddingModel);
    }

    @Test
    void shouldEmbedOnFirstSave() {
        Category category = Fixtures.category(Fixtures.UNIT);
        when(neo4jRepository.findByIdAndUnitId(category.getId(), Fixtures.UNIT)).thenReturn(Optional.empty());
        when(embeddingModel.embed(category.toEmbeddableText())).thenReturn(VECTOR);
        when(neo4jRepository.save(any(CategoryNode.class))).thenAnswer(i -> i.getArgument(0));

        Category result = repository.save(category);

        assertEquals(category.getId(), result.getId());
        verify(neo4jRepository).save(argThatCarries(VECTOR, category.toEmbeddableText()));
    }

    @Test
    void shouldReuseTheStoredVectorWhenTheTextDidNotChange() {
        Category category = Fixtures.category(Fixtures.UNIT);
        CategoryNode stored = mapper.toNode(category);
        stored.setEmbedding(VECTOR);
        stored.setEmbeddedText(category.toEmbeddableText());
        when(neo4jRepository.findByIdAndUnitId(category.getId(), Fixtures.UNIT)).thenReturn(Optional.of(stored));
        when(neo4jRepository.save(any(CategoryNode.class))).thenAnswer(i -> i.getArgument(0));

        repository.save(category);

        verify(embeddingModel, never()).embed(anyString());
    }

    @Test
    void shouldReEmbedWhenTheTextChanged() {
        Category category = Fixtures.category(Fixtures.UNIT);
        CategoryNode stored = mapper.toNode(category);
        stored.setEmbedding(VECTOR);
        stored.setEmbeddedText("outro texto qualquer");
        when(neo4jRepository.findByIdAndUnitId(category.getId(), Fixtures.UNIT)).thenReturn(Optional.of(stored));
        when(embeddingModel.embed(category.toEmbeddableText())).thenReturn(VECTOR);
        when(neo4jRepository.save(any(CategoryNode.class))).thenAnswer(i -> i.getArgument(0));

        repository.save(category);

        verify(embeddingModel).embed(category.toEmbeddableText());
    }

    @Test
    void shouldFindByIdWithinTheUnit() {
        UUID id = UUID.randomUUID();
        when(neo4jRepository.findByIdAndUnitId(id, Fixtures.UNIT))
                .thenReturn(Optional.of(mapper.toNode(Fixtures.category(id, Fixtures.UNIT))));

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
                .thenReturn(List.of(mapper.toNode(Fixtures.category(Fixtures.UNIT))));

        List<Category> result = repository.findAll(Fixtures.UNIT);

        assertEquals(1, result.size());
        assertEquals(Fixtures.UNIT, result.get(0).getUnitId());
    }

    @Test
    void shouldDeleteWithinTheUnit() {
        UUID id = UUID.randomUUID();

        repository.deleteById(Fixtures.UNIT, id);

        verify(neo4jRepository).deleteByIdAndUnitId(id, Fixtures.UNIT);
    }

    private CategoryNode argThatCarries(float[] vector, String text) {
        return org.mockito.ArgumentMatchers.argThat(node -> {
            assertArrayEquals(vector, node.getEmbedding());
            assertEquals(text, node.getEmbeddedText());
            return true;
        });
    }
}
