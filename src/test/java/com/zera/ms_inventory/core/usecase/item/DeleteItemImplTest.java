package com.zera.ms_inventory.core.usecase.item;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Event;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.exception.InvalidItemTransitionException;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.EventType;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.repository.EventRepository;
import com.zera.ms_inventory.core.repository.ItemRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteItemImplTest {

    @Mock private ItemRepository itemRepository;
    @Mock private EventRepository eventRepository;

    /** A exclusao virou logica na ZERA-247: o no continua no grafo, o status vira REMOVED. */
    @Test
    void shouldRemoveLogicallyAndKeepWhereTheItemCameFrom() {
        UUID id = UUID.randomUUID();
        Item item = Fixtures.item(id, Fixtures.UNIT);
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(item));

        new DeleteItemImpl(itemRepository, eventRepository).execute(Fixtures.UNIT, id, Fixtures.OPERATOR);

        assertThat(item.getStatus()).isEqualTo(ItemStatus.REMOVED);
        verify(itemRepository).save(item);
        verify(itemRepository, never()).deleteById(any(), any());
        ArgumentCaptor<Event> event = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(event.capture());
        assertThat(event.getValue().getType()).isEqualTo(EventType.REMOVED);
        assertThat(event.getValue().getFromStatus()).isEqualTo(ItemStatus.IN_STOCK);
    }

    /** O descarte encerra a vida do item: nao da para remover depois dele. */
    @Test
    void shouldRefuseToRemoveADisposedItem() {
        UUID id = UUID.randomUUID();
        Item item = Fixtures.item(id, Fixtures.UNIT);
        item.restoreStatus(ItemStatus.DISPOSED);
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(item));

        DeleteItemImpl useCase = new DeleteItemImpl(itemRepository, eventRepository);

        assertThatThrownBy(() -> useCase.execute(Fixtures.UNIT, id, Fixtures.MANAGER))
                .isInstanceOf(InvalidItemTransitionException.class);
        verify(itemRepository, never()).save(any());
    }

    @Test
    void shouldNotRemoveFromAnotherUnit() {
        UUID id = UUID.randomUUID();
        when(itemRepository.findById(Fixtures.OTHER_UNIT, id)).thenReturn(Optional.empty());

        DeleteItemImpl useCase = new DeleteItemImpl(itemRepository, eventRepository);

        assertThatThrownBy(() -> useCase.execute(Fixtures.OTHER_UNIT, id, Fixtures.MANAGER))
                .isInstanceOf(ItemNotFoundException.class);
        verify(itemRepository, never()).save(any());
    }
}
