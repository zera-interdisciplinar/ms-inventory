package com.zera.ms_inventory.core.usecase.item;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.exception.ItemIdInUseException;
import com.zera.ms_inventory.core.domain.exception.ModelNotFoundException;
import com.zera.ms_inventory.core.repository.ItemRepository;
import com.zera.ms_inventory.core.repository.ModelRepository;
import com.zera.ms_inventory.core.usecase.model.CreateModel;

@Service
public class CreateItemImpl implements CreateItem {
    private final ItemRepository itemRepository;
    private final ModelRepository modelRepository;
    private final CreateModel createModel;
    private final DisplayCodeGenerator displayCodeGenerator;

    public CreateItemImpl(ItemRepository itemRepository, ModelRepository modelRepository, CreateModel createModel,
                          DisplayCodeGenerator displayCodeGenerator) {
        this.itemRepository = itemRepository;
        this.modelRepository = modelRepository;
        this.createModel = createModel;
        this.displayCodeGenerator = displayCodeGenerator;
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
                command.status(), command.unitId(), model, null, command.manufacturingYear(),
                command.usageIntensity(), command.serialNumber(), command.acquiredAt());
        item.describe(command.name(), command.condition(), command.hasDamages(), command.damages(), command.notes());
        item.registerBy(command.actor());
        item.assignDisplayCode(displayCodeGenerator.next(command.unitId()));
        return new CreateItemResult(itemRepository.save(item), true);
    }
}
