package com.zera.ms_inventory.core.usecase.item;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Event;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
import com.zera.ms_inventory.core.repository.EventRepository;
import com.zera.ms_inventory.core.repository.ItemRepository;

@Service
public class ListItemEventsImpl implements ListItemEvents {

    private final ItemRepository itemRepository;
    private final EventRepository eventRepository;

    public ListItemEventsImpl(ItemRepository itemRepository, EventRepository eventRepository) {
        this.itemRepository = itemRepository;
        this.eventRepository = eventRepository;
    }

    // confirma o item antes: historico de item inexistente e 404, nao pagina vazia
    @Override
    public PageResult<Event> execute(UUID unitId, UUID itemId, Pagination pagination) {
        if (itemRepository.findById(unitId, itemId).isEmpty()) {
            throw new ItemNotFoundException(itemId);
        }
        return eventRepository.findPageByItem(unitId, itemId, pagination);
    }
}
