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
public class RejectItemImpl implements RejectItem {

    private final ItemRepository itemRepository;
    private final ModelRepository modelRepository;
    private final EventRepository eventRepository;
    private final ItemNotifier notifier;

    public RejectItemImpl(ItemRepository itemRepository, ModelRepository modelRepository,
                          EventRepository eventRepository, ItemNotifier notifier) {
        this.itemRepository = itemRepository;
        this.modelRepository = modelRepository;
        this.eventRepository = eventRepository;
        this.notifier = notifier;
    }

    @Override
    @Transactional
    public Item execute(UUID unitId, UUID id, String reason, Actor actor) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason is required to reject an item");
        }
        Item item = itemRepository.findById(unitId, id)
                .orElseThrow(() -> new ItemNotFoundException(id));

        Event event = item.transitionTo(ItemStatus.REJECTED, EventType.REJECTED, reason, actor);

        rejectModelWithoutOtherItems(item, reason, actor);

        Item saved = itemRepository.save(item);
        eventRepository.save(event);
        notifier.itemRejected(saved, actor, reason);
        return saved;
    }

    /**
     * O modelo so cai junto se existir apenas por causa deste item; com outros itens usando o
     * modelo, reprovar todos eles por tabela seria destrutivo demais.
     */
    private void rejectModelWithoutOtherItems(Item item, String reason, Actor actor) {
        Model model = item.getModel();
        if (model == null || !model.isPendingApproval()) {
            return;
        }
        if (itemRepository.countByModel(item.getUnitId(), model.getId()) <= 1) {
            model.rejectBy(actor, reason);
            modelRepository.save(model);
        }
    }
}
