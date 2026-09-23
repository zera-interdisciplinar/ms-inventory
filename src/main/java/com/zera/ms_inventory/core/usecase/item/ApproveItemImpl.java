package com.zera.ms_inventory.core.usecase.item;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zera.ms_inventory.core.domain.entity.Event;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.EventType;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.repository.EventRepository;
import com.zera.ms_inventory.core.repository.ItemNotifier;
import com.zera.ms_inventory.core.repository.ItemRepository;
import com.zera.ms_inventory.core.repository.ModelRepository;

@Service
public class ApproveItemImpl implements ApproveItem {

    private final ItemRepository itemRepository;
    private final ModelRepository modelRepository;
    private final EventRepository eventRepository;
    private final ItemNotifier notifier;

    public ApproveItemImpl(ItemRepository itemRepository, ModelRepository modelRepository,
                           EventRepository eventRepository, ItemNotifier notifier) {
        this.itemRepository = itemRepository;
        this.modelRepository = modelRepository;
        this.eventRepository = eventRepository;
        this.notifier = notifier;
    }

    /** Aprovar o item aprova o modelo cadastrado junto com ele, decisao de produto da v1. */
    @Override
    @Transactional
    public Item execute(UUID unitId, UUID id, Actor actor) {
        Item item = itemRepository.findById(unitId, id)
                .orElseThrow(() -> new ItemNotFoundException(id));

        Event event = item.transitionTo(ItemStatus.IN_STOCK, EventType.APPROVED, null, actor);

        Model model = item.getModel();
        if (model != null && model.isPendingApproval()) {
            model.approveBy(actor);
            modelRepository.save(model);
        }

        Item saved = itemRepository.save(item);
        eventRepository.save(event);
        notifier.itemApproved(saved, actor);
        return saved;
    }
}
