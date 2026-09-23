package com.zera.ms_inventory.core.usecase.disposal;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.exception.InvalidItemTransitionException;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.DestinationType;
import com.zera.ms_inventory.core.domain.valueobject.EventType;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.repository.DisposalRepository;
import com.zera.ms_inventory.core.repository.EventRepository;
import com.zera.ms_inventory.core.repository.ItemRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateDisposalImplTest {

    @Mock private ItemRepository itemRepository;
    @Mock private DisposalRepository disposalRepository;
    @Mock private EventRepository eventRepository;

    private CreateDisposalImpl useCase() {
        return new CreateDisposalImpl(itemRepository, disposalRepository, eventRepository);
    }

    private Model modelWeighing(Double kg) {
        return new Model(UUID.randomUUID(), Fixtures.UNIT, "Notebook X1", "Acme", 24, 60, Set.of(), kg, null,
                Fixtures.category(Fixtures.UNIT));
    }

    private Item stockedItem(UUID id, Double weight) {
        Item item = Fixtures.item(id, Fixtures.UNIT, modelWeighing(weight));
        item.assignDisplayCode("10000" + Math.abs(id.hashCode() % 10));
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(item));
        return item;
    }

    private CreateDisposalCommand command(List<UUID> ids, DestinationType destination) {
        return new CreateDisposalCommand(Fixtures.UNIT, destination, "places/abc", "Ecoponto",
                LocalDate.now(), null, ids, Fixtures.OPERATOR);
    }

    /** O peso vem do modelo e e congelado: corrigir o modelo depois nao reescreve os kg. */
    @Test
    void shouldDisposeTheItemsFreezingTheWeightFromTheModel() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        Item first = stockedItem(a, 2.5);
        Item second = stockedItem(b, 1.5);
        when(disposalRepository.save(any(Disposal.class))).thenAnswer(i -> i.getArgument(0));

        Disposal disposal = useCase().execute(command(List.of(a, b), DestinationType.RECYCLING));

        assertThat(first.getStatus()).isEqualTo(ItemStatus.DISPOSED);
        assertThat(second.getStatus()).isEqualTo(ItemStatus.DISPOSED);
        assertThat(disposal.getItems()).extracting("weightKg").containsExactlyInAnyOrder(2.5, 1.5);
        assertThat(disposal.totalWeightKg()).isEqualTo(4.0);
        verify(itemRepository, times(2)).save(any(Item.class));
        verify(eventRepository, times(2)).save(any(Event.class));
    }

    @Test
    void shouldRecordTheDestinationInEachItemHistory() {
        UUID id = UUID.randomUUID();
        stockedItem(id, 1.0);
        when(disposalRepository.save(any(Disposal.class))).thenAnswer(i -> i.getArgument(0));

        useCase().execute(command(List.of(id), DestinationType.LANDFILL));

        ArgumentCaptor<Event> event = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(event.capture());
        assertThat(event.getValue().getType()).isEqualTo(EventType.DISPOSED);
        assertThat(event.getValue().getToStatus()).isEqualTo(ItemStatus.DISPOSED);
        assertThat(event.getValue().getReason()).isEqualTo("LANDFILL");
    }

    @Test
    void shouldAcceptAModelWithoutEstimatedWeight() {
        UUID id = UUID.randomUUID();
        stockedItem(id, null);
        when(disposalRepository.save(any(Disposal.class))).thenAnswer(i -> i.getArgument(0));

        Disposal disposal = useCase().execute(command(List.of(id), DestinationType.DONATION));

        assertThat(disposal.getItems()).singleElement()
                .satisfies(item -> assertThat(item.weightKg()).isNull());
        assertThat(disposal.totalWeightKg()).isZero();
    }

    /** O mesmo item enviado duas vezes na selecao nao pode ser descartado em dobro. */
    @Test
    void shouldIgnoreRepeatedItemsInTheSelection() {
        UUID id = UUID.randomUUID();
        stockedItem(id, 3.0);
        when(disposalRepository.save(any(Disposal.class))).thenAnswer(i -> i.getArgument(0));

        Disposal disposal = useCase().execute(command(List.of(id, id, id), DestinationType.RECYCLING));

        assertThat(disposal.getItems()).hasSize(1);
        assertThat(disposal.totalWeightKg()).isEqualTo(3.0);
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    /** Descartar um rascunho nao faz sentido: a maquina de estados barra e nada e gravado. */
    @Test
    void shouldRefuseAnItemThatCannotBeDisposed() {
        UUID id = UUID.randomUUID();
        Item item = stockedItem(id, 1.0);
        item.restoreStatus(ItemStatus.DRAFT);
        CreateDisposalImpl useCase = useCase();
        CreateDisposalCommand command = command(List.of(id), DestinationType.RECYCLING);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(InvalidItemTransitionException.class);
        verify(disposalRepository, never()).save(any());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void shouldRefuseAnItemFromAnotherUnit() {
        UUID id = UUID.randomUUID();
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.empty());
        CreateDisposalImpl useCase = useCase();
        CreateDisposalCommand command = command(List.of(id), DestinationType.RECYCLING);

        assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(ItemNotFoundException.class);
        verify(disposalRepository, never()).save(any());
    }

    @Test
    void shouldRefuseAnEmptySelection() {
        CreateDisposalImpl useCase = useCase();
        CreateDisposalCommand command = command(List.of(), DestinationType.RECYCLING);

        assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(IllegalArgumentException.class);
    }
}
