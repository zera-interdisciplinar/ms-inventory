package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.ModelNode;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.ModelMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModelRepositoryImplTest {

    @Mock
    private ModelNeo4jRepository neo4jRepository;

    private final ModelMapper mapper = new ModelMapper();

    private ModelRepositoryImpl repository;

    @Test
    void shouldSaveModel() {
        repository = new ModelRepositoryImpl(neo4jRepository, mapper);
        UUID id = UUID.randomUUID();
        Model model = new Model(id, "Laptop X1", "Acme", 24, 60, Set.of("Lithium"));
        when(neo4jRepository.save(any(ModelNode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Model result = repository.save(model);

        assertEquals(model.getId(), result.getId());
        assertEquals(model.getName(), result.getName());
    }

    @Test
    void shouldFindById() {
        repository = new ModelRepositoryImpl(neo4jRepository, mapper);
        UUID id = UUID.randomUUID();
        ModelNode node = mapper.toNode(new Model(id, "Laptop X1", "Acme", 24, 60, Set.of("Lithium")));
        when(neo4jRepository.findById(id)).thenReturn(Optional.of(node));

        Optional<Model> result = repository.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        repository = new ModelRepositoryImpl(neo4jRepository, mapper);
        UUID id = UUID.randomUUID();
        when(neo4jRepository.findById(id)).thenReturn(Optional.empty());

        assertTrue(repository.findById(id).isEmpty());
    }

    @Test
    void shouldFindAll() {
        repository = new ModelRepositoryImpl(neo4jRepository, mapper);
        ModelNode node = mapper.toNode(new Model(UUID.randomUUID(), "Laptop X1", "Acme", 24, 60, Set.of("Lithium")));
        when(neo4jRepository.findAll()).thenReturn(List.of(node));

        List<Model> result = repository.findAll();

        assertEquals(1, result.size());
        assertEquals("Laptop X1", result.get(0).getName());
    }

    @Test
    void shouldDeleteById() {
        repository = new ModelRepositoryImpl(neo4jRepository, mapper);
        UUID id = UUID.randomUUID();

        repository.deleteById(id);

        verify(neo4jRepository).deleteById(id);
    }
}
