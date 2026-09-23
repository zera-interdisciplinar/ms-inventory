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
public class DeleteItemImpl implements DeleteItem {

    private final ItemRepository itemRepository;
    private final EventRepository eventRepository;

    public DeleteItemImpl(ItemRepository itemRepository, EventRepository eventRepository) {
        this.itemRepository = itemRepository;
        this.eventRepository = eventRepository;
    }

    /**
     * A exclusao deixou de apagar o no: o item vai para REMOVED, sai das listagens e o gestor pode
     * restaurar. O evento guarda de onde ele saiu, que e para onde a restauracao devolve. Item ja
     * descartado nao e removivel, e o dominio responde 409.
     */
    @Override
    @Transactional
    public void execute(UUID unitId, UUID id, Actor actor) {
        Item item = itemRepository.findById(unitId, id)
                .orElseThrow(() -> new ItemNotFoundException(id));
        Event event = item.transitionTo(ItemStatus.REMOVED, EventType.REMOVED, null, actor);
        itemRepository.save(item);
        eventRepository.save(event);
    }
}
