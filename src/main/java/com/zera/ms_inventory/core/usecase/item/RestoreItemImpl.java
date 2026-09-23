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
public class RestoreItemImpl implements RestoreItem {

    private final ItemRepository itemRepository;
    private final EventRepository eventRepository;

    public RestoreItemImpl(ItemRepository itemRepository, EventRepository eventRepository) {
        this.itemRepository = itemRepository;
        this.eventRepository = eventRepository;
    }

    /**
     * Devolve o item ao estado de onde ele foi removido, lido do proprio historico: o evento
     * REMOVED guarda em fromStatus de onde ele saiu. Sem esse evento (item migrado de antes do
     * historico) o destino e o estoque.
     */
    @Override
    @Transactional
    public Item execute(UUID unitId, UUID id, Actor actor) {
        Item item = itemRepository.findById(unitId, id)
                .orElseThrow(() -> new ItemNotFoundException(id));

        ItemStatus previous = eventRepository.findLastByItemAndType(unitId, id, EventType.REMOVED)
                .map(Event::getFromStatus)
                .filter(ItemStatus::isActive)
                .orElse(ItemStatus.IN_STOCK);

        Event event = item.transitionTo(previous, EventType.RESTORED, null, actor);
        Item saved = itemRepository.save(item);
        eventRepository.save(event);
        return saved;
    }
}
