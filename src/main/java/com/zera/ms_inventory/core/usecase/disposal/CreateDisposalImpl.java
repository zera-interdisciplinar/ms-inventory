package com.zera.ms_inventory.core.usecase.disposal;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zera.ms_inventory.core.domain.entity.Disposal;
import com.zera.ms_inventory.core.domain.entity.Event;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.DisposedItem;
import com.zera.ms_inventory.core.domain.valueobject.EventType;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.repository.DisposalRepository;
import com.zera.ms_inventory.core.repository.EventRepository;
import com.zera.ms_inventory.core.repository.ItemRepository;

@Service
public class CreateDisposalImpl implements CreateDisposal {

    private final ItemRepository itemRepository;
    private final DisposalRepository disposalRepository;
    private final EventRepository eventRepository;

    public CreateDisposalImpl(ItemRepository itemRepository, DisposalRepository disposalRepository,
                              EventRepository eventRepository) {
        this.itemRepository = itemRepository;
        this.disposalRepository = disposalRepository;
        this.eventRepository = eventRepository;
    }

    /**
     * Itens e descarte na mesma transacao: um item marcado como descartado sem o registro do
     * destino sumiria do estoque sem deixar para onde foi. O peso vem do modelo e e congelado
     * aqui, porque corrigir o modelo depois nao pode reescrever o historico de kg descartados.
     */
    @Override
    @Transactional
    public Disposal execute(CreateDisposalCommand command) {
        List<UUID> itemIds = List.copyOf(new LinkedHashSet<>(command.itemIds()));
        if (itemIds.isEmpty()) {
            throw new IllegalArgumentException("inform at least one item to dispose");
        }

        List<DisposedItem> disposed = new ArrayList<>();
        List<Event> events = new ArrayList<>();
        for (UUID itemId : itemIds) {
            Item item = itemRepository.findById(command.unitId(), itemId)
                    .orElseThrow(() -> new ItemNotFoundException(itemId));
            events.add(item.transitionTo(ItemStatus.DISPOSED, EventType.DISPOSED,
                    command.destination().name(), command.actor()));
            itemRepository.save(item);
            disposed.add(new DisposedItem(item.getId(), item.getDisplayCode(), item.getName(), weightOf(item)));
        }

        Disposal disposal = disposalRepository.save(Disposal.register(command.unitId(), command.destination(),
                command.placeId(), command.placeName(), command.disposedAt(), command.notes(), disposed,
                command.actor()));
        events.forEach(eventRepository::save);
        return disposal;
    }

    /** O peso e estimado no modelo e herdado pelo item; modelo sem peso nao soma nos indicadores. */
    private static Double weightOf(Item item) {
        return item.getModel() != null ? item.getModel().getEstimatedWeightKg() : null;
    }
}
