package com.zera.ms_inventory.infrastructure.http.request;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import com.zera.ms_inventory.core.domain.valueobject.DamageType;
import com.zera.ms_inventory.core.domain.valueobject.ItemCondition;
import com.zera.ms_inventory.core.usecase.item.UpdateItemCommand;

/** Todos opcionais: campo ausente (ou null) nao e alterado; texto vazio apaga notes e serialNumber. */
public record UpdateItemRequest(
        @Size(min = 1, max = 120) String name,
        ItemCondition condition,
        Boolean hasDamages,
        Set<DamageType> damages,
        @Size(max = 500) String notes,
        @Size(max = 120) String serialNumber,
        LocalDate acquiredAt,
        Integer manufacturingYear,
        @Min(0) @Max(10) Integer usageIntensity
) {
    public UpdateItemCommand toCommand(UUID unitId, UUID id) {
        return new UpdateItemCommand(unitId, id, name, condition, hasDamages, damages, notes, serialNumber,
                acquiredAt, manufacturingYear, usageIntensity);
    }
}
