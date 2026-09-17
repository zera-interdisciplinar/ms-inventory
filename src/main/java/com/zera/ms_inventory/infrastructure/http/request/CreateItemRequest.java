package com.zera.ms_inventory.infrastructure.http.request;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.core.domain.valueobject.DamageType;
import com.zera.ms_inventory.core.domain.valueobject.ItemCondition;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.domain.valueobject.UsageIntensity;
import com.zera.ms_inventory.core.usecase.item.CreateItemCommand;

public record CreateItemRequest(
        @NotBlank String barcode,
        @NotNull ItemStatus status,
        @NotNull UUID modelId,
        Integer manufacturingYear,
        UsageIntensity usageIntensity,
        String serialNumber,
        LocalDate acquiredAt,
        @Size(max = 120) String name,
        ItemCondition condition,
        Boolean hasDamages,
        Set<DamageType> damages,
        @Size(max = 500) String notes
) {
    /** unitId vem do header X-Unit-Id e o autor do token, nunca do corpo. */
    public CreateItemCommand toCommand(UUID unitId, Actor actor) {
        return new CreateItemCommand(new Barcode(barcode), status, unitId, modelId, manufacturingYear,
                usageIntensity, serialNumber, acquiredAt, name, condition, hasDamages, damages,
                notes, actor);
    }
}
