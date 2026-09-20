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
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.exception.IncompleteItemException;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.ApprovalStatus;
import com.zera.ms_inventory.core.domain.valueobject.EventType;
import com.zera.ms_inventory.core.domain.valueobject.ItemCondition;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.repository.EventRepository;
import com.zera.ms_inventory.core.repository.ItemRepository;
import com.zera.ms_inventory.core.repository.ModelRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmitItemImplTest {

    @Mock private ItemRepository itemRepository;
    @Mock private ModelRepository modelRepository;
    @Mock private EventRepository eventRepository;

    private SubmitItemImpl useCase() {
        return new SubmitItemImpl(itemRepository, modelRepository, eventRepository);
    }

    /** Rascunho completo: tem os 7 obrigatorios do cadastro, foto inclusa. */
    private Item completeDraft(UUID id, Model model) {
        Item item = Fixtures.item(id, Fixtures.UNIT, model);
        item.restoreStatus(ItemStatus.DRAFT);
        item.describe("Notebook", ItemCondition.USED, false, Set.of(), null);
        item.attachPhoto("photos/" + id + ".jpg");
        return item;
    }

    @Test
    void shouldSendTheOperatorDraftToApproval() {
        UUID id = UUID.randomUUID();
        Item item = completeDraft(id, Fixtures.model(Fixtures.UNIT));
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);

        Item result = useCase().execute(Fixtures.UNIT, id, Fixtures.OPERATOR);

        assertThat(result.getStatus()).isEqualTo(ItemStatus.PENDING_APPROVAL);
        ArgumentCaptor<Event> event = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(event.capture());
        assertThat(event.getValue().getType()).isEqualTo(EventType.SUBMITTED);
        assertThat(event.getValue().getToStatus()).isEqualTo(ItemStatus.PENDING_APPROVAL);
    }

    /** O gestor e quem aprovaria, entao o item dele entra direto no estoque com o modelo aprovado. */
    @Test
    void shouldPutTheManagerDraftStraightIntoStockAndApproveItsModel() {
        UUID id = UUID.randomUUID();
        Model model = Fixtures.model(Fixtures.UNIT);
        model.registerBy(Fixtures.OPERATOR);
        Item item = completeDraft(id, model);
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);

        Item result = useCase().execute(Fixtures.UNIT, id, Fixtures.MANAGER);

        assertThat(result.getStatus()).isEqualTo(ItemStatus.IN_STOCK);
        assertThat(model.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
        verify(modelRepository).save(model);
    }

    @Test
    void shouldNotTouchAModelThatIsAlreadyApproved() {
        UUID id = UUID.randomUUID();
        Model model = Fixtures.model(Fixtures.UNIT);
        model.registerBy(Fixtures.MANAGER);
        Item item = completeDraft(id, model);
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);

        useCase().execute(Fixtures.UNIT, id, Fixtures.MANAGER);

        verify(modelRepository, never()).save(any());
    }

    @Test
    void shouldRefuseToSubmitAndListWhatIsMissing() {
        UUID id = UUID.randomUUID();
        Item item = Fixtures.item(id, Fixtures.UNIT);
        item.restoreStatus(ItemStatus.DRAFT);
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(item));

        SubmitItemImpl useCase = useCase();

        assertThatThrownBy(() -> useCase.execute(Fixtures.UNIT, id, Fixtures.OPERATOR))
                .isInstanceOf(IncompleteItemException.class)
                .satisfies(e -> assertThat(((IncompleteItemException) e).getMissingFields())
                        .containsExactly("name", "condition", "hasDamages", "photo"));
        verify(itemRepository, never()).save(any());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenTheItemIsNotInTheUnit() {
        UUID id = UUID.randomUUID();
        when(itemRepository.findById(Fixtures.OTHER_UNIT, id)).thenReturn(Optional.empty());

        SubmitItemImpl useCase = useCase();

        assertThatThrownBy(() -> useCase.execute(Fixtures.OTHER_UNIT, id, Fixtures.OPERATOR))
                .isInstanceOf(ItemNotFoundException.class);
    }
}
