package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Event;
import com.zera.ms_inventory.core.domain.valueobject.EventType;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.EventNode;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.EventMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventRepositoryImplTest {

    @Mock
    private EventNeo4jRepository neo4jRepository;

    private final EventMapper mapper = new EventMapper();

    private EventRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new EventRepositoryImpl(neo4jRepository, mapper);
    }

    private Event event(UUID itemId) {
        return Event.of(itemId, Fixtures.UNIT, EventType.APPROVED, ItemStatus.PENDING_APPROVAL,
                ItemStatus.IN_STOCK, "ok", Fixtures.MANAGER);
    }

    /** Historico orfao nao serve: o no e a ligacao com o item saem na mesma chamada. */
    @Test
    void shouldSaveTheEventAndAttachItToTheItem() {
        UUID itemId = UUID.randomUUID();
        Event event = event(itemId);
        when(neo4jRepository.save(any(EventNode.class))).thenAnswer(i -> i.getArgument(0));

        Event saved = repository.save(event);

        assertThat(saved.getId()).isEqualTo(event.getId());
        assertThat(saved.getType()).isEqualTo(EventType.APPROVED);
        verify(neo4jRepository).attachToItem(Fixtures.UNIT, itemId, event.getId());
    }

    @Test
    void shouldPageTheHistoryOfTheItem() {
        UUID itemId = UUID.randomUUID();
        when(neo4jRepository.countByItem(Fixtures.UNIT, itemId)).thenReturn(7L);
        when(neo4jRepository.findPageByItem(Fixtures.UNIT, itemId, 4L, 2))
                .thenReturn(List.of(mapper.toNode(event(itemId))));

        PageResult<Event> result = repository.findPageByItem(Fixtures.UNIT, itemId, new Pagination(2, 2));

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(7);
        assertThat(result.totalPages()).isEqualTo(4);
    }

    /** Sem eventos nao vale ir ao banco buscar a pagina. */
    @Test
    void shouldReturnAnEmptyPageWithoutQueryingWhenThereIsNoHistory() {
        UUID itemId = UUID.randomUUID();
        when(neo4jRepository.countByItem(Fixtures.UNIT, itemId)).thenReturn(0L);

        PageResult<Event> result = repository.findPageByItem(Fixtures.UNIT, itemId, new Pagination(0, 20));

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
        verify(neo4jRepository, never()).findPageByItem(any(), any(), anyLong(), anyInt());
    }

    @Test
    void shouldListTheWholeHistoryOfTheItem() {
        UUID itemId = UUID.randomUUID();
        when(neo4jRepository.findAllByItem(Fixtures.UNIT, itemId))
                .thenReturn(List.of(mapper.toNode(event(itemId)), mapper.toNode(event(itemId))));

        List<Event> result = repository.findAllByItem(Fixtures.UNIT, itemId);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(e -> e.getItemId().equals(itemId));
    }
}
