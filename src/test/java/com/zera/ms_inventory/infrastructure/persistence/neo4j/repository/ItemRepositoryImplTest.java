package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.ItemNode;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.ItemMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRepositoryImplTest {

    @Mock
    private ItemNeo4jRepository neo4jRepository;

    private final ItemMapper mapper = new ItemMapper();

    private ItemRepositoryImpl repository;

    private Item sampleItem(UUID id) {
        return new Item(id, new Barcode("123456"), ItemStatus.OK, UUID.randomUUID(), LocalDateTime.now(),
                12, 5, "SN-001", LocalDate.now());
    }

    @Test
    void shouldSaveItem() {
        repository = new ItemRepositoryImpl(neo4jRepository, mapper);
        UUID id = UUID.randomUUID();
        Item item = sampleItem(id);
        when(neo4jRepository.save(any(ItemNode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Item result = repository.save(item);

        assertEquals(item.getId(), result.getId());
        assertEquals(item.getBarcode(), result.getBarcode());
    }

    @Test
    void shouldFindById() {
        repository = new ItemRepositoryImpl(neo4jRepository, mapper);
        UUID id = UUID.randomUUID();
        ItemNode node = mapper.toNode(sampleItem(id));
        when(neo4jRepository.findById(id)).thenReturn(Optional.of(node));

        Optional<Item> result = repository.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        repository = new ItemRepositoryImpl(neo4jRepository, mapper);
        UUID id = UUID.randomUUID();
        when(neo4jRepository.findById(id)).thenReturn(Optional.empty());

        assertTrue(repository.findById(id).isEmpty());
    }

    @Test
    void shouldFindAll() {
        repository = new ItemRepositoryImpl(neo4jRepository, mapper);
        ItemNode node = mapper.toNode(sampleItem(UUID.randomUUID()));
        when(neo4jRepository.findAll()).thenReturn(List.of(node));

        List<Item> result = repository.findAll();

        assertEquals(1, result.size());
        assertEquals("123456", result.get(0).getBarcode().getValue());
    }

    @Test
    void shouldDeleteById() {
        repository = new ItemRepositoryImpl(neo4jRepository, mapper);
        UUID id = UUID.randomUUID();

        repository.deleteById(id);

        verify(neo4jRepository).deleteById(id);
    }
}
