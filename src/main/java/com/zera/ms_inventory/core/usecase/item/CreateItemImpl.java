package com.zera.ms_inventory.core.usecase.item;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zera.ms_inventory.core.domain.entity.Event;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.exception.ItemIdInUseException;
import com.zera.ms_inventory.core.domain.exception.ModelNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.EventType;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.repository.EventRepository;
import com.zera.ms_inventory.core.repository.ItemRepository;
import com.zera.ms_inventory.core.repository.ModelRepository;
import com.zera.ms_inventory.core.usecase.model.CreateModel;

@Service
public class CreateItemImpl implements CreateItem {
    private final ItemRepository itemRepository;
    private final ModelRepository modelRepository;
    private final CreateModel createModel;
    private final DisplayCodeGenerator displayCodeGenerator;
    private final EventRepository eventRepository;

    public CreateItemImpl(ItemRepository itemRepository, ModelRepository modelRepository, CreateModel createModel,
                          DisplayCodeGenerator displayCodeGenerator, EventRepository eventRepository) {
        this.itemRepository = itemRepository;
        this.modelRepository = modelRepository;
        this.createModel = createModel;
        this.displayCodeGenerator = displayCodeGenerator;
        this.eventRepository = eventRepository;
    }

    // modelo novo e item ficam na mesma transacao: se o item falhar, o modelo nao sobra
    @Override
    @Transactional
    public CreateItemResult execute(CreateItemCommand command) {
        if (command.id() != null) {
            Optional<Item> alreadyCreated = itemRepository.findById(command.unitId(), command.id());
            if (alreadyCreated.isPresent()) {
                return new CreateItemResult(alreadyCreated.get(), false);
            }
            // o mesmo id em outra unidade seria sobrescrito pelo save
            if (itemRepository.existsAnyWithId(command.id())) {
                throw new ItemIdInUseException(command.id());
            }
        }

        Model model = command.modelId() != null
                // resolvido pelo par (modelId, unitId): modelo de outra unidade nao existe daqui
                ? modelRepository.findById(command.unitId(), command.modelId())
                        .orElseThrow(() -> new ModelNotFoundException(command.modelId()))
                : createModel.execute(command.newModel());

        Item item = new Item(command.id() != null ? command.id() : UUID.randomUUID(), command.barcode(),
                ItemStatus.DRAFT, command.unitId(), model, null, command.manufacturingYear(),
                command.usageIntensity(), command.serialNumber(), command.acquiredAt());
        item.describe(command.name(), command.condition(), command.hasDamages(), command.damages(), command.notes());
        item.registerBy(command.actor());
        item.assignDisplayCode(displayCodeGenerator.next(command.unitId()));
        item.restoreStatus(initialStatus(item, command.actor()));
        Item saved = itemRepository.save(item);
        // abre o historico: o primeiro passo do item e o proprio cadastro
        eventRepository.save(Event.of(saved.getId(), saved.getUnitId(), EventType.CREATED, null, saved.getStatus(),
                null, command.actor()));
        return new CreateItemResult(saved, true);
    }

    /**
     * Cadastro incompleto vira rascunho. Completo, o do gestor ja entra no estoque e o do operario
     * espera aprovacao. Como a foto sobe em outra chamada, o caminho normal do app e nascer em
     * DRAFT e so depois passar pelo submit.
     */
    private static ItemStatus initialStatus(Item item, Actor actor) {
        if (!item.isReadyToSubmit()) {
            return ItemStatus.DRAFT;
        }
        return actor != null && actor.isManager() ? ItemStatus.IN_STOCK : ItemStatus.PENDING_APPROVAL;
    }
}
