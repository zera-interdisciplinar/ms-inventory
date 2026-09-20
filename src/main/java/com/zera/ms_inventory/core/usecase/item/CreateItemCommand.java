package com.zera.ms_inventory.core.usecase.item;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.core.domain.valueobject.DamageType;
import com.zera.ms_inventory.core.domain.valueobject.ItemCondition;
import com.zera.ms_inventory.core.usecase.model.CreateModelCommand;

/**
 * Cadastro de item. {@code id} opcional vem do app (reenvio offline e idempotente). O modelo e um
 * existente ({@code modelId}) ou um novo criado junto ({@code newModel}), nunca os dois.
 */
public record CreateItemCommand(
        UUID id,
        Barcode barcode,
        UUID unitId,
        UUID modelId,
        CreateModelCommand newModel,
        Integer manufacturingYear,
        Integer usageIntensity,
        String serialNumber,
        LocalDate acquiredAt,
        String name,
        ItemCondition condition,
        Boolean hasDamages,
        Set<DamageType> damages,
        String notes,
        Actor actor
) {}
