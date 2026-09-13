package com.zera.ms_inventory.core.usecase.item;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.DamageType;
import com.zera.ms_inventory.core.repository.ItemRepository;

/** Pop-up "Editar o item": aplica so os campos enviados. */
@Service
public class UpdateItemImpl implements UpdateItem {
    private final ItemRepository itemRepository;

    public UpdateItemImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public Item execute(UpdateItemCommand command) {
        Item item = itemRepository.findById(command.unitId(), command.id())
                .orElseThrow(() -> new ItemNotFoundException(command.id()));

        Boolean hasDamages = command.hasDamages() != null ? command.hasDamages() : item.getHasDamages();
        Set<DamageType> damages = command.damages() != null ? command.damages() : item.getDamages();
        // responder "nao possui danos" limpa a lista anterior quando o app nao manda outra
        if (Boolean.FALSE.equals(command.hasDamages()) && command.damages() == null) {
            damages = Set.of();
        }
        item.describe(
                command.name() != null ? command.name() : item.getName(),
                command.condition() != null ? command.condition() : item.getCondition(),
                hasDamages,
                damages,
                command.notes() != null ? blankToNull(command.notes()) : item.getNotes());

        if (command.serialNumber() != null) {
            item.updateSerialNumber(blankToNull(command.serialNumber()));
        }
        if (command.acquiredAt() != null) {
            item.updateAcquiredAt(command.acquiredAt());
        }
        if (command.manufacturingYear() != null) {
            item.updateManufacturingYear(command.manufacturingYear());
        }
        if (command.usageIntensity() != null) {
            item.updateUsageIntensity(command.usageIntensity());
        }
        return itemRepository.save(item);
    }

    // texto vazio e a forma de o app apagar um campo opcional
    private static String blankToNull(String value) {
        return value.isBlank() ? null : value;
    }
}
