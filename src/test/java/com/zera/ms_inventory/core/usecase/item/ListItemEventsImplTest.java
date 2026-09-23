package com.zera.ms_inventory.core.usecase.item;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Event;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.EventType;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
import com.zera.ms_inventory.core.repository.EventRepository;
import com.zera.ms_inventory.core.repository.ItemRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class ListItemEventsImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private EventRepository eventRepository;

    @Test
    void shouldReturnTheHistoryPageOfTheItem() {
        UUID id = UUID.randomUUID();
        Pagination pagination = new Pagination(0, 20);
        Event event = Event.of(id, Fixtures.UNIT, EventType.CREATED, null, ItemStatus.IN_STOCK, null,
                Fixtures.OPERATOR);
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(Fixtures.item(id, Fixtures.UNIT)));
        when(eventRepository.findPageByItem(Fixtures.UNIT, id, pagination))
                .thenReturn(new PageResult<>(List.of(event), 0, 20, 1));

        PageResult<Event> result = new ListItemEventsImpl(itemRepository, eventRepository)
                .execute(Fixtures.UNIT, id, pagination);

        assertThat(result.content()).containsExactly(event);
        assertThat(result.totalElements()).isEqualTo(1);
    }

    /** Historico de item que nao existe naquela unidade e 404, nao pagina vazia. */
    @Test
    void shouldThrowWhenTheItemIsNotInTheUnit() {
        UUID id = UUID.randomUUID();
        when(itemRepository.findById(Fixtures.OTHER_UNIT, id)).thenReturn(Optional.empty());

        ListItemEventsImpl useCase = new ListItemEventsImpl(itemRepository, eventRepository);

        assertThatThrownBy(() -> useCase.execute(Fixtures.OTHER_UNIT, id, new Pagination(0, 20)))
                .isInstanceOf(ItemNotFoundException.class);
        verify(eventRepository, never()).findPageByItem(any(), any(), any());
    }

    @Test
    void shouldForwardThePaginationAsInformed() {
        UUID id = UUID.randomUUID();
        Pagination pagination = new Pagination(3, 5);
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(Fixtures.item(id, Fixtures.UNIT)));
        when(eventRepository.findPageByItem(eq(Fixtures.UNIT), eq(id), eq(pagination)))
                .thenReturn(new PageResult<>(List.of(), 3, 5, 0));

        new ListItemEventsImpl(itemRepository, eventRepository).execute(Fixtures.UNIT, id, pagination);

        verify(eventRepository).findPageByItem(Fixtures.UNIT, id, pagination);
    }
}
