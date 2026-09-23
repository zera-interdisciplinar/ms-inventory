package com.zera.ms_inventory.core.usecase.item;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.valueobject.DamageType;
import com.zera.ms_inventory.core.domain.valueobject.ItemCondition;

/** Edicao parcial do item: campo nulo significa "nao alterar". */
public record UpdateItemCommand(
        UUID unitId,
        UUID id,
        String name,
        ItemCondition condition,
        Boolean hasDamages,
        Set<DamageType> damages,
        String notes,
        String serialNumber,
        LocalDate acquiredAt,
        Integer manufacturingYear,
        Integer usageIntensity
) {}
