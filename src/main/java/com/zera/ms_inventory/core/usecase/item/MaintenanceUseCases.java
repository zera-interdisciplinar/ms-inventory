package com.zera.ms_inventory.core.usecase.item;

import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zera.ms_inventory.core.domain.entity.Event;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.DamageType;
import com.zera.ms_inventory.core.domain.valueobject.EventType;
import com.zera.ms_inventory.core.domain.valueobject.ItemCondition;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.repository.EventRepository;
import com.zera.ms_inventory.core.repository.ItemRepository;

/**
 * Os tres passos da manutencao. Operario e gestor executam todos eles; o que restringe e a maquina
 * de estados, nao o papel: so sai do estoque para a manutencao, so avalia o que voltou dela.
 */
@Service
public class MaintenanceUseCases implements StartMaintenance, FinishMaintenance, EvaluateItem {

    private final ItemRepository itemRepository;
    private final EventRepository eventRepository;

    public MaintenanceUseCases(ItemRepository itemRepository, EventRepository eventRepository) {
        this.itemRepository = itemRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional
    public Item execute(UUID unitId, UUID id, String reason, Actor actor) {
        return move(unitId, id, ItemStatus.IN_MAINTENANCE, EventType.MAINTENANCE_STARTED, reason, actor, null);
    }

    @Override
    @Transactional
    public Item execute(UUID unitId, UUID id, Actor actor) {
        return move(unitId, id, ItemStatus.AWAITING_EVALUATION, EventType.MAINTENANCE_FINISHED, null, actor, null);
    }

    @Override
    @Transactional
    public Item execute(UUID unitId, UUID id, ItemCondition condition, Boolean hasDamages,
                        Set<DamageType> damages, Actor actor) {
        if (condition == null) {
            throw new IllegalArgumentException("condition is required to evaluate an item");
        }
        return move(unitId, id, ItemStatus.IN_STOCK, EventType.EVALUATED, condition.name(), actor,
                item -> item.evaluateCondition(condition, hasDamages, damages));
    }

    private Item move(UUID unitId, UUID id, ItemStatus target, EventType type, String reason, Actor actor,
                      java.util.function.Consumer<Item> beforeSave) {
        Item item = itemRepository.findById(unitId, id)
                .orElseThrow(() -> new ItemNotFoundException(id));
        Event event = item.transitionTo(target, type, reason, actor);
        if (beforeSave != null) {
            beforeSave.accept(item);
        }
        Item saved = itemRepository.save(item);
        eventRepository.save(event);
        return saved;
    }
}
