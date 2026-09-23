package com.zera.ms_inventory.core.usecase.item;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zera.ms_inventory.core.domain.entity.Event;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.exception.IncompleteItemException;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.EventType;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.repository.EventRepository;
import com.zera.ms_inventory.core.repository.ItemRepository;
import com.zera.ms_inventory.core.repository.ModelRepository;

@Service
public class SubmitItemImpl implements SubmitItem {

    private final ItemRepository itemRepository;
    private final ModelRepository modelRepository;
    private final EventRepository eventRepository;

    public SubmitItemImpl(ItemRepository itemRepository, ModelRepository modelRepository,
                          EventRepository eventRepository) {
        this.itemRepository = itemRepository;
        this.modelRepository = modelRepository;
        this.eventRepository = eventRepository;
    }

    /**
     * Envia o rascunho. O do operario vai para a fila do gestor; o do gestor ja entra no estoque,
     * porque ele e quem aprovaria de qualquer forma, e o modelo criado junto entra aprovado.
     */
    @Override
    @Transactional
    public Item execute(UUID unitId, UUID id, Actor actor) {
        Item item = itemRepository.findById(unitId, id)
                .orElseThrow(() -> new ItemNotFoundException(id));

        List<String> missing = item.missingRequiredFields();
        if (!missing.isEmpty()) {
            throw new IncompleteItemException(id, missing);
        }

        boolean manager = actor != null && actor.isManager();
        ItemStatus target = manager ? ItemStatus.IN_STOCK : ItemStatus.PENDING_APPROVAL;
        Event event = item.transitionTo(target, EventType.SUBMITTED, null, actor);

        if (manager) {
            approveModelOf(item, actor);
        }
        Item saved = itemRepository.save(item);
        eventRepository.save(event);
        return saved;
    }

    private void approveModelOf(Item item, Actor actor) {
        Model model = item.getModel();
        if (model != null && model.isPendingApproval()) {
            model.approveBy(actor);
            modelRepository.save(model);
        }
    }
}
