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
class ReturnItemImplTest {

    @Mock private ItemRepository itemRepository;
    @Mock private EventRepository eventRepository;

    private ReturnItemImpl useCase() {
        return new ReturnItemImpl(itemRepository, eventRepository);
    }

    private Item itemAt(UUID id, ItemStatus status) {
        Item item = Fixtures.item(id, Fixtures.UNIT);
        item.restoreStatus(status);
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(item));
        return item;
    }

    @Test
    void shouldMoveToInStockAndRecordTheEventWhenItemIsInUse() {
        UUID id = UUID.randomUUID();
        Item item = itemAt(id, ItemStatus.IN_USE);
        when(itemRepository.save(item)).thenReturn(item);

        Item result = useCase().execute(Fixtures.UNIT, id, Fixtures.OPERATOR);

        assertThat(result.getStatus()).isEqualTo(ItemStatus.IN_STOCK);
        ArgumentCaptor<Event> event = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(event.capture());
        assertThat(event.getValue().getType()).isEqualTo(EventType.LOAN_RETURNED);
        assertThat(event.getValue().getFromStatus()).isEqualTo(ItemStatus.IN_USE);
        assertThat(event.getValue().getToStatus()).isEqualTo(ItemStatus.IN_STOCK);
        assertThat(event.getValue().getReason()).isNull();
    }

    @Test
    void shouldRejectWhenItemIsInStock() {
        UUID id = UUID.randomUUID();
        itemAt(id, ItemStatus.IN_STOCK);

        assertThatThrownBy(() -> useCase().execute(Fixtures.UNIT, id, Fixtures.OPERATOR))
                .isInstanceOf(InvalidItemTransitionException.class);
        verify(itemRepository, never()).save(any());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenNotFoundInThisUnit() {
        UUID id = UUID.randomUUID();
        when(itemRepository.findById(Fixtures.OTHER_UNIT, id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase().execute(Fixtures.OTHER_UNIT, id, Fixtures.OPERATOR))
                .isInstanceOf(ItemNotFoundException.class);
    }
}
