package com.zera.ms_inventory.core.usecase.disposal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zera.ms_inventory.core.domain.entity.Disposal;
import com.zera.ms_inventory.core.domain.entity.Event;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.exception.DisposalAlreadyCancelledException;
import com.zera.ms_inventory.core.domain.exception.DisposalNotFoundException;
import com.zera.ms_inventory.core.domain.exception.InvalidItemTransitionException;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.DisposedItem;
import com.zera.ms_inventory.core.domain.valueobject.EventType;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.repository.DisposalRepository;
import com.zera.ms_inventory.core.repository.EventRepository;
import com.zera.ms_inventory.core.repository.ItemRepository;

@Service
public class CancelDisposalImpl implements CancelDisposal {

    private final DisposalRepository disposalRepository;
    private final ItemRepository itemRepository;
    private final EventRepository eventRepository;

    public CancelDisposalImpl(DisposalRepository disposalRepository, ItemRepository itemRepository,
                              EventRepository eventRepository) {
        this.disposalRepository = disposalRepository;
        this.itemRepository = itemRepository;
        this.eventRepository = eventRepository;
    }

    /**
     * Valida todos os itens antes de gravar: se um nao puder voltar, nada muda. O destino vem do
     * evento DISPOSED (de onde o item saiu), nao de um chute.
     */
    @Override
    @Transactional
    public void execute(UUID unitId, UUID id, Actor actor) {
        Disposal disposal = disposalRepository.findById(unitId, id)
                .orElseThrow(() -> new DisposalNotFoundException(id));
        if (disposal.isCancelled()) {
            throw new DisposalAlreadyCancelledException(id);
        }

        List<PendingRestore> pending = new ArrayList<>();
        for (DisposedItem disposed : disposal.getItems()) {
            Item item = itemRepository.findById(unitId, disposed.itemId())
                    .orElseThrow(() -> new ItemNotFoundException(disposed.itemId()));
            if (item.getStatus() != ItemStatus.DISPOSED) {
                throw new InvalidItemTransitionException(item.getId(), item.getStatus(),
                        restoreTarget(unitId, item.getId()));
            }
            pending.add(new PendingRestore(item, restoreTarget(unitId, item.getId())));
        }

        for (PendingRestore restore : pending) {
            Event event = restore.item().transitionTo(restore.target(), EventType.DISPOSAL_CANCELLED, null, actor);
            itemRepository.save(restore.item());
            eventRepository.save(event);
        }

        disposal.cancel();
        disposalRepository.save(disposal);
    }

    private ItemStatus restoreTarget(UUID unitId, UUID itemId) {
        return eventRepository.findLastByItemAndType(unitId, itemId, EventType.DISPOSED)
                .map(Event::getFromStatus)
                .filter(status -> status == ItemStatus.IN_STOCK || status == ItemStatus.AWAITING_EVALUATION)
                .orElse(ItemStatus.IN_STOCK);
    }

    private record PendingRestore(Item item, ItemStatus target) {
    }
}
