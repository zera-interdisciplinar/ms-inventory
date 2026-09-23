package com.zera.ms_inventory.core.usecase.item;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zera.ms_inventory.core.domain.entity.Event;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.EventType;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.repository.EventRepository;
import com.zera.ms_inventory.core.repository.ItemRepository;

@Service
public class UpdateItemStatusImpl implements UpdateItemStatus {
    private final ItemRepository itemRepository;
    private final EventRepository eventRepository;

    public UpdateItemStatusImpl(ItemRepository itemRepository, EventRepository eventRepository) {
        this.itemRepository = itemRepository;
        this.eventRepository = eventRepository;
    }

    // item e evento na mesma transacao: um status sem o passo correspondente no historico e um furo
    @Override
    @Transactional
    public Item execute(UUID unitId, UUID id, ItemStatus status, Actor actor) {
        Item item = itemRepository.findById(unitId, id)
                .orElseThrow(() -> new ItemNotFoundException(id));
        Event event = item.transitionTo(status, EventType.STATUS_CHANGED, null, actor);
        Item saved = itemRepository.save(item);
        eventRepository.save(event);
        return saved;
    }
}
