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

        // Todo item nasce rascunho, mesmo com o cadastro ja completo: quem tira o item do DRAFT e
        // sempre o submit. Ja existiu aqui um atalho que mandava o cadastro completo direto para
        // PENDING_APPROVAL/IN_STOCK, inalcancavel na pratica porque a foto era obrigatoria e so
        // sobe em outra chamada. Ao tornar a foto opcional o atalho passou a disparar, e o app
        // tomava 409 no submit seguinte (PENDING_APPROVAL -> PENDING_APPROVAL nao e transicao
        // valida). Manter o DRAFT preserva o contrato de create -> foto opcional -> submit.
        Item item = new Item(command.id() != null ? command.id() : UUID.randomUUID(), command.barcode(),
                ItemStatus.DRAFT, command.unitId(), model, null, command.manufacturingYear(),
                command.usageIntensity(), command.serialNumber(), command.acquiredAt());
        item.describe(command.name(), command.condition(), command.hasDamages(), command.damages(), command.notes());
        item.registerBy(command.actor());
        item.assignDisplayCode(displayCodeGenerator.next(command.unitId()));
        Item saved = itemRepository.save(item);
        // abre o historico: o primeiro passo do item e o proprio cadastro
        eventRepository.save(Event.of(saved.getId(), saved.getUnitId(), EventType.CREATED, null, saved.getStatus(),
                null, command.actor()));
        return new CreateItemResult(saved, true);
    }

}
