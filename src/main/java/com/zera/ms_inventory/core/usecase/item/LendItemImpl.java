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

/**
 * Empresta o item. Na v1 o emprestimo e so o estado: nao guardamos quem pegou nem prazo, porque o
 * sistema nao tem papel para quem retira o item (v2). Operario e gestor podem executar; quem
 * restringe e a maquina de estados.
 */
@Service
public class LendItemImpl implements LendItem {

    private final ItemRepository itemRepository;
    private final EventRepository eventRepository;

    public LendItemImpl(ItemRepository itemRepository, EventRepository eventRepository) {
        this.itemRepository = itemRepository;
        this.eventRepository = eventRepository;
    }

    // item e evento na mesma transacao: um status sem o passo correspondente no historico e um furo
    @Override
    @Transactional
    public Item execute(UUID unitId, UUID id, Actor actor) {
        Item item = itemRepository.findById(unitId, id)
                .orElseThrow(() -> new ItemNotFoundException(id));
        Event event = item.transitionTo(ItemStatus.IN_USE, EventType.LOAN_STARTED, null, actor);
        Item saved = itemRepository.save(item);
        eventRepository.save(event);
        return saved;
    }
}
