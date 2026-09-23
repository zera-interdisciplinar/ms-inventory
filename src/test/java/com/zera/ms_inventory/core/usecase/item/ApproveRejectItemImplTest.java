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
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.exception.InvalidItemTransitionException;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.ApprovalStatus;
import com.zera.ms_inventory.core.domain.valueobject.EventType;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.repository.EventRepository;
import com.zera.ms_inventory.core.repository.ItemNotifier;
import com.zera.ms_inventory.core.repository.ItemRepository;
import com.zera.ms_inventory.core.repository.ModelRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApproveRejectItemImplTest {

    @Mock private ItemRepository itemRepository;
    @Mock private ModelRepository modelRepository;
    @Mock private EventRepository eventRepository;
    @Mock private ItemNotifier notifier;

    private ApproveItemImpl approve() {
        return new ApproveItemImpl(itemRepository, modelRepository, eventRepository, notifier);
    }

    private RejectItemImpl reject() {
        return new RejectItemImpl(itemRepository, modelRepository, eventRepository, notifier);
    }

    private Item pendingItem(UUID id, Model model) {
        Item item = Fixtures.item(id, Fixtures.UNIT, model);
        item.restoreStatus(ItemStatus.PENDING_APPROVAL);
        return item;
    }

    private Model pendingModel() {
        Model model = Fixtures.model(Fixtures.UNIT);
        model.registerBy(Fixtures.OPERATOR);
        return model;
    }

    // ---- aprovacao ----

    /** Decisao de produto da v1: aprovar o item aprova o modelo cadastrado junto. */
    @Test
    void shouldApproveTheItemAndItsPendingModel() {
        UUID id = UUID.randomUUID();
        Model model = pendingModel();
        Item item = pendingItem(id, model);
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);

        Item result = approve().execute(Fixtures.UNIT, id, Fixtures.MANAGER);

        assertThat(result.getStatus()).isEqualTo(ItemStatus.IN_STOCK);
        assertThat(model.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
        verify(modelRepository).save(model);
        verify(notifier).itemApproved(item, Fixtures.MANAGER);
        ArgumentCaptor<Event> event = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(event.capture());
        assertThat(event.getValue().getType()).isEqualTo(EventType.APPROVED);
    }

    @Test
    void shouldRefuseToApproveAnItemThatIsNotWaitingForApproval() {
        UUID id = UUID.randomUUID();
        Item item = Fixtures.item(id, Fixtures.UNIT);
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(item));

        ApproveItemImpl useCase = approve();

        assertThatThrownBy(() -> useCase.execute(Fixtures.UNIT, id, Fixtures.MANAGER))
                .isInstanceOf(InvalidItemTransitionException.class);
        verify(notifier, never()).itemApproved(any(), any());
    }

    // ---- reprovacao ----

    @Test
    void shouldRejectTheItemAndTheModelThatOnlyItUses() {
        UUID id = UUID.randomUUID();
        Model model = pendingModel();
        Item item = pendingItem(id, model);
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(item));
        when(itemRepository.countByModel(Fixtures.UNIT, model.getId())).thenReturn(1L);
        when(itemRepository.save(item)).thenReturn(item);

        Item result = reject().execute(Fixtures.UNIT, id, "Foto ilegivel", Fixtures.MANAGER);

        assertThat(result.getStatus()).isEqualTo(ItemStatus.REJECTED);
        assertThat(model.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED);
        assertThat(model.getRejectionReason()).isEqualTo("Foto ilegivel");
        verify(notifier).itemRejected(item, Fixtures.MANAGER, "Foto ilegivel");
    }

    /** Reprovar um modelo usado por outros itens derrubaria itens que ninguem revisou. */
    @Test
    void shouldKeepTheModelWhenOtherItemsUseIt() {
        UUID id = UUID.randomUUID();
        Model model = pendingModel();
        Item item = pendingItem(id, model);
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(item));
        when(itemRepository.countByModel(Fixtures.UNIT, model.getId())).thenReturn(4L);
        when(itemRepository.save(item)).thenReturn(item);

        reject().execute(Fixtures.UNIT, id, "Foto ilegivel", Fixtures.MANAGER);

        assertThat(model.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);
        verify(modelRepository, never()).save(any());
    }

    @Test
    void shouldRequireAReasonToReject() {
        RejectItemImpl useCase = reject();
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> useCase.execute(Fixtures.UNIT, id, "   ", Fixtures.MANAGER))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> useCase.execute(Fixtures.UNIT, id, null, Fixtures.MANAGER))
                .isInstanceOf(IllegalArgumentException.class);
        verify(itemRepository, never()).findById(any(), any());
    }

    @Test
    void shouldThrowWhenTheItemIsNotInTheUnit() {
        UUID id = UUID.randomUUID();
        when(itemRepository.findById(Fixtures.OTHER_UNIT, id)).thenReturn(Optional.empty());

        RejectItemImpl useCase = reject();

        assertThatThrownBy(() -> useCase.execute(Fixtures.OTHER_UNIT, id, "motivo", Fixtures.MANAGER))
                .isInstanceOf(ItemNotFoundException.class);
    }
}
