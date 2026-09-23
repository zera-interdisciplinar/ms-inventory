package com.zera.ms_inventory.core.usecase.item;

import java.util.Optional;
import java.util.Set;
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
import com.zera.ms_inventory.core.domain.valueobject.DamageType;
import com.zera.ms_inventory.core.domain.valueobject.EventType;
import com.zera.ms_inventory.core.domain.valueobject.ItemCondition;
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
class MaintenanceUseCasesTest {

    @Mock private ItemRepository itemRepository;
    @Mock private EventRepository eventRepository;

    private MaintenanceUseCases useCase() {
        return new MaintenanceUseCases(itemRepository, eventRepository);
    }

    private Item itemAt(UUID id, ItemStatus status) {
        Item item = Fixtures.item(id, Fixtures.UNIT);
        item.restoreStatus(status);
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(item));
        return item;
    }

    /** Passos que chegam ao save precisam do retorno; os que sao recusados nunca chegam la. */
    private Item savedItemAt(UUID id, ItemStatus status) {
        Item item = itemAt(id, status);
        when(itemRepository.save(item)).thenReturn(item);
        return item;
    }

    @Test
    void shouldStartMaintenanceKeepingTheReasonInTheHistory() {
        UUID id = UUID.randomUUID();
        Item item = savedItemAt(id, ItemStatus.IN_STOCK);

        Item result = useCase().execute(Fixtures.UNIT, id, "Tela piscando", Fixtures.OPERATOR);

        assertThat(result.getStatus()).isEqualTo(ItemStatus.IN_MAINTENANCE);
        ArgumentCaptor<Event> event = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(event.capture());
        assertThat(event.getValue().getType()).isEqualTo(EventType.MAINTENANCE_STARTED);
        assertThat(event.getValue().getReason()).isEqualTo("Tela piscando");
        assertThat(item.getLastEventAt()).isNotNull();
    }

    @Test
    void shouldFinishMaintenanceIntoEvaluation() {
        UUID id = UUID.randomUUID();
        savedItemAt(id, ItemStatus.IN_MAINTENANCE);

        Item result = useCase().execute(Fixtures.UNIT, id, Fixtures.OPERATOR);

        assertThat(result.getStatus()).isEqualTo(ItemStatus.AWAITING_EVALUATION);
        ArgumentCaptor<Event> event = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(event.capture());
        assertThat(event.getValue().getType()).isEqualTo(EventType.MAINTENANCE_FINISHED);
    }

    @Test
    void shouldEvaluateBackIntoStockRecordingTheCondition() {
        UUID id = UUID.randomUUID();
        Item item = savedItemAt(id, ItemStatus.AWAITING_EVALUATION);

        Item result = useCase().execute(Fixtures.UNIT, id, ItemCondition.USED, null, null, Fixtures.OPERATOR);

        assertThat(result.getStatus()).isEqualTo(ItemStatus.IN_STOCK);
        assertThat(result.getCondition()).isEqualTo(ItemCondition.USED);
        ArgumentCaptor<Event> event = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(event.capture());
        assertThat(event.getValue().getType()).isEqualTo(EventType.EVALUATED);
        assertThat(event.getValue().getReason()).isEqualTo("USED");
        assertThat(item.getStatus()).isEqualTo(ItemStatus.IN_STOCK);
    }

    @Test
    void shouldLetTheEvaluationCorrectTheDamages() {
        UUID id = UUID.randomUUID();
        Item item = savedItemAt(id, ItemStatus.AWAITING_EVALUATION);
        item.describe("Notebook", ItemCondition.DAMAGED, true, Set.of(DamageType.OXIDATION), null);

        Item result = useCase().execute(Fixtures.UNIT, id, ItemCondition.USED, false, Set.of(), Fixtures.MANAGER);

        assertThat(result.getCondition()).isEqualTo(ItemCondition.USED);
        assertThat(result.getHasDamages()).isFalse();
        assertThat(result.getDamages()).isEmpty();
    }

    /** Silencio sobre os danos mantem o que ja estava registrado. */
    @Test
    void shouldKeepTheDamagesWhenTheEvaluationDoesNotMentionThem() {
        UUID id = UUID.randomUUID();
        Item item = savedItemAt(id, ItemStatus.AWAITING_EVALUATION);
        item.describe("Notebook", ItemCondition.DAMAGED, true, Set.of(DamageType.OXIDATION), null);

        Item result = useCase().execute(Fixtures.UNIT, id, ItemCondition.SEMI_DAMAGED, null, null, Fixtures.MANAGER);

        assertThat(result.getDamages()).containsExactly(DamageType.OXIDATION);
        assertThat(result.getHasDamages()).isTrue();
    }

    @Test
    void shouldRequireTheConditionToEvaluate() {
        MaintenanceUseCases useCase = useCase();
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> useCase.execute(Fixtures.UNIT, id, null, null, null, Fixtures.MANAGER))
                .isInstanceOf(IllegalArgumentException.class);
        verify(itemRepository, never()).findById(any(), any());
    }

    /** A maquina de estados e quem protege: nao se avalia o que nao voltou da manutencao. */
    @Test
    void shouldRefuseStepsOutOfOrder() {
        UUID inStock = UUID.randomUUID();
        itemAt(inStock, ItemStatus.IN_STOCK);
        MaintenanceUseCases useCase = useCase();

        assertThatThrownBy(() -> useCase.execute(Fixtures.UNIT, inStock, ItemCondition.USED, null, null,
                Fixtures.OPERATOR)).isInstanceOf(InvalidItemTransitionException.class);
        assertThatThrownBy(() -> useCase.execute(Fixtures.UNIT, inStock, Fixtures.OPERATOR))
                .isInstanceOf(InvalidItemTransitionException.class);
        verify(eventRepository, never()).save(any());
    }
}
