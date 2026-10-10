package com.zera.ms_inventory.core.usecase.disposal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Disposal;
import com.zera.ms_inventory.core.domain.entity.Event;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.exception.DisposalAlreadyCancelledException;
import com.zera.ms_inventory.core.domain.exception.DisposalNotFoundException;
import com.zera.ms_inventory.core.domain.exception.InvalidItemTransitionException;
import com.zera.ms_inventory.core.domain.valueobject.DestinationType;
import com.zera.ms_inventory.core.domain.valueobject.DisposedItem;
import com.zera.ms_inventory.core.domain.valueobject.EventType;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.repository.DisposalRepository;
import com.zera.ms_inventory.core.repository.EventRepository;
import com.zera.ms_inventory.core.repository.ItemRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelDisposalImplTest {

    @Mock private DisposalRepository disposalRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private EventRepository eventRepository;

    private CancelDisposalImpl useCase() {
        return new CancelDisposalImpl(disposalRepository, itemRepository, eventRepository);
    }

    private Item disposedItem(UUID id) {
        Item item = Fixtures.item(id, Fixtures.UNIT);
        item.restoreStatus(ItemStatus.DISPOSED);
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(item));
        return item;
    }

    private void disposedFrom(UUID id, ItemStatus origin) {
        when(eventRepository.findLastByItemAndType(Fixtures.UNIT, id, EventType.DISPOSED))
                .thenReturn(Optional.of(Event.of(id, Fixtures.UNIT, EventType.DISPOSED, origin,
                        ItemStatus.DISPOSED, null, Fixtures.OPERATOR)));
    }

    private Disposal disposalWith(UUID... itemIds) {
        List<DisposedItem> items = java.util.Arrays.stream(itemIds)
                .map(id -> new DisposedItem(id, "100001", "Notebook", 1.0))
                .toList();
        Disposal disposal = Disposal.register(Fixtures.UNIT, DestinationType.RECYCLING, null, null, null, null,
                items, Fixtures.OPERATOR);
        when(disposalRepository.findById(Fixtures.UNIT, disposal.getId())).thenReturn(Optional.of(disposal));
        return disposal;
    }

    @Test
    void shouldRestoreAnItemThatWasInStock() {
        UUID itemId = UUID.randomUUID();
        Item item = disposedItem(itemId);
        disposedFrom(itemId, ItemStatus.IN_STOCK);
        Disposal disposal = disposalWith(itemId);
        when(itemRepository.save(item)).thenReturn(item);
        when(disposalRepository.save(disposal)).thenReturn(disposal);

        useCase().execute(Fixtures.UNIT, disposal.getId(), Fixtures.OPERATOR);

        assertThat(item.getStatus()).isEqualTo(ItemStatus.IN_STOCK);
        assertThat(disposal.isCancelled()).isTrue();
        ArgumentCaptor<Event> event = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(event.capture());
        assertThat(event.getValue().getType()).isEqualTo(EventType.DISPOSAL_CANCELLED);
        verify(disposalRepository).save(disposal);
    }

    @Test
    void shouldRestoreAnItemThatWasAwaitingEvaluation() {
        UUID itemId = UUID.randomUUID();
        Item item = disposedItem(itemId);
        disposedFrom(itemId, ItemStatus.AWAITING_EVALUATION);
        Disposal disposal = disposalWith(itemId);
        when(itemRepository.save(item)).thenReturn(item);
        when(disposalRepository.save(disposal)).thenReturn(disposal);

        useCase().execute(Fixtures.UNIT, disposal.getId(), Fixtures.OPERATOR);

        assertThat(item.getStatus()).isEqualTo(ItemStatus.AWAITING_EVALUATION);
    }

    @Test
    void shouldThrowWhenTheDisposalIsInAnotherUnit() {
        UUID id = UUID.randomUUID();
        when(disposalRepository.findById(Fixtures.OTHER_UNIT, id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase().execute(Fixtures.OTHER_UNIT, id, Fixtures.OPERATOR))
                .isInstanceOf(DisposalNotFoundException.class);
        verify(itemRepository, never()).save(any());
        verify(disposalRepository, never()).save(any());
    }

    @Test
    void shouldConflictWhenTheDisposalIsAlreadyCancelled() {
        UUID itemId = UUID.randomUUID();
        Disposal disposal = disposalWith(itemId);
        disposal.cancel();

        assertThatThrownBy(() -> useCase().execute(Fixtures.UNIT, disposal.getId(), Fixtures.OPERATOR))
                .isInstanceOf(DisposalAlreadyCancelledException.class);
        verify(itemRepository, never()).save(any());
        verify(disposalRepository, never()).save(any());
    }

    @Test
    void shouldNotPersistAnythingWhenAnItemCannotReturn() {
        UUID ok = UUID.randomUUID();
        UUID stuck = UUID.randomUUID();
        disposedItem(ok);
        disposedFrom(ok, ItemStatus.IN_STOCK);
        Item notDisposed = Fixtures.item(stuck, Fixtures.UNIT);
        when(itemRepository.findById(Fixtures.UNIT, stuck)).thenReturn(Optional.of(notDisposed));
        Disposal disposal = disposalWith(ok, stuck);

        assertThatThrownBy(() -> useCase().execute(Fixtures.UNIT, disposal.getId(), Fixtures.OPERATOR))
                .isInstanceOf(InvalidItemTransitionException.class);
        verify(itemRepository, never()).save(any());
        verify(eventRepository, never()).save(any());
        verify(disposalRepository, never()).save(any());
        assertThat(disposal.isCancelled()).isFalse();
    }
}
