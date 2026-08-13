package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.core.domain.entity.Category;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.CategoryNode;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.CategoryMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryRepositoryImplTest {

    @Mock
    private CategoryNeo4jRepository neo4jRepository;

    private final CategoryMapper mapper = new CategoryMapper();

    private CategoryRepositoryImpl repository;

    @Test
    void shouldSaveCategory() {
        repository = new CategoryRepositoryImpl(neo4jRepository, mapper);
        UUID id = UUID.randomUUID();
        Category category = new Category(id, "Electronics", "Devices");
        when(neo4jRepository.save(any(CategoryNode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Category result = repository.save(category);

        assertEquals(category.getId(), result.getId());
        assertEquals(category.getName(), result.getName());
    }

    @Test
    void shouldFindById() {
        repository = new CategoryRepositoryImpl(neo4jRepository, mapper);
        UUID id = UUID.randomUUID();
        CategoryNode node = mapper.toNode(new Category(id, "Electronics", "Devices"));
        when(neo4jRepository.findById(id)).thenReturn(Optional.of(node));

        Optional<Category> result = repository.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        repository = new CategoryRepositoryImpl(neo4jRepository, mapper);
        UUID id = UUID.randomUUID();
        when(neo4jRepository.findById(id)).thenReturn(Optional.empty());

        assertTrue(repository.findById(id).isEmpty());
    }

    @Test
    void shouldFindAll() {
        repository = new CategoryRepositoryImpl(neo4jRepository, mapper);
        CategoryNode node = mapper.toNode(new Category(UUID.randomUUID(), "Electronics", "Devices"));
        when(neo4jRepository.findAll()).thenReturn(List.of(node));

        List<Category> result = repository.findAll();

        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).getName());
    }

    @Test
    void shouldDeleteById() {
        repository = new CategoryRepositoryImpl(neo4jRepository, mapper);
        UUID id = UUID.randomUUID();

        repository.deleteById(id);

        verify(neo4jRepository).deleteById(id);
    }
}
