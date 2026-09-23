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
class RestoreItemImplTest {

    @Mock private ItemRepository itemRepository;
    @Mock private EventRepository eventRepository;

    private RestoreItemImpl useCase() {
        return new RestoreItemImpl(itemRepository, eventRepository);
    }

    private Item removedItem(UUID id) {
        Item item = Fixtures.item(id, Fixtures.UNIT);
        item.restoreStatus(ItemStatus.REMOVED);
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);
        return item;
    }

    private void removedFrom(UUID id, ItemStatus origin) {
        when(eventRepository.findLastByItemAndType(Fixtures.UNIT, id, EventType.REMOVED))
                .thenReturn(Optional.of(Event.of(id, Fixtures.UNIT, EventType.REMOVED, origin,
                        ItemStatus.REMOVED, null, Fixtures.OPERATOR)));
    }

    /** O historico diz de onde o item saiu, entao a restauracao devolve exatamente para la. */
    @Test
    void shouldRestoreToTheStatusTheItemWasRemovedFrom() {
        UUID id = UUID.randomUUID();
        removedItem(id);
        removedFrom(id, ItemStatus.AWAITING_EVALUATION);

        Item result = useCase().execute(Fixtures.UNIT, id, Fixtures.MANAGER);

        assertThat(result.getStatus()).isEqualTo(ItemStatus.AWAITING_EVALUATION);
        ArgumentCaptor<Event> event = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(event.capture());
        assertThat(event.getValue().getType()).isEqualTo(EventType.RESTORED);
        assertThat(event.getValue().getFromStatus()).isEqualTo(ItemStatus.REMOVED);
    }

    @Test
    void shouldRestoreADraftBackToDraft() {
        UUID id = UUID.randomUUID();
        removedItem(id);
        removedFrom(id, ItemStatus.DRAFT);

        assertThat(useCase().execute(Fixtures.UNIT, id, Fixtures.MANAGER).getStatus())
                .isEqualTo(ItemStatus.DRAFT);
    }

    /** Item migrado de antes do historico nao tem evento de remocao; o destino seguro e o estoque. */
    @Test
    void shouldFallBackToStockWithoutARemovalEvent() {
        UUID id = UUID.randomUUID();
        removedItem(id);
        when(eventRepository.findLastByItemAndType(Fixtures.UNIT, id, EventType.REMOVED))
                .thenReturn(Optional.empty());

        assertThat(useCase().execute(Fixtures.UNIT, id, Fixtures.MANAGER).getStatus())
                .isEqualTo(ItemStatus.IN_STOCK);
    }

    @Test
    void shouldRefuseToRestoreAnItemThatWasNotRemoved() {
        UUID id = UUID.randomUUID();
        Item item = Fixtures.item(id, Fixtures.UNIT);
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(item));

        RestoreItemImpl useCase = useCase();

        assertThatThrownBy(() -> useCase.execute(Fixtures.UNIT, id, Fixtures.MANAGER))
                .isInstanceOf(InvalidItemTransitionException.class);
        verify(itemRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenTheItemIsNotInTheUnit() {
        UUID id = UUID.randomUUID();
        when(itemRepository.findById(Fixtures.OTHER_UNIT, id)).thenReturn(Optional.empty());

        RestoreItemImpl useCase = useCase();

        assertThatThrownBy(() -> useCase.execute(Fixtures.OTHER_UNIT, id, Fixtures.MANAGER))
                .isInstanceOf(ItemNotFoundException.class);
    }
}
