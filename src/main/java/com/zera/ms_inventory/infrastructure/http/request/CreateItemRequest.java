package com.zera.ms_inventory.infrastructure.http.request;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.core.domain.valueobject.DamageType;
import com.zera.ms_inventory.core.domain.valueobject.ItemCondition;
import com.zera.ms_inventory.core.usecase.item.CreateItemCommand;

/** Informe modelId de um modelo existente ou model para criar o modelo junto (exatamente um). */
public record CreateItemRequest(
        UUID id,
        @NotBlank String barcode,
        UUID modelId,
        @Valid NewItemModelRequest model,
        Integer manufacturingYear,
        @Min(0) @Max(10) Integer usageIntensity,
        String serialNumber,
        LocalDate acquiredAt,
        @Size(max = 120) String name,
        ItemCondition condition,
        Boolean hasDamages,
        Set<DamageType> damages,
        @Size(max = 500) String notes
) {
    /** unitId vem do header X-Unit-Id e o autor do token, nunca do corpo. */
    @AssertTrue(message = "inform exactly one of modelId or model")
    public boolean isExactlyOneModelInformed() {
        return (modelId == null) != (model == null);
    }

    public CreateItemCommand toCommand(UUID unitId, Actor actor) {
        // o status nao vem do cliente: quem decide e a regra de rascunho/aprovacao (ZERA-244)
        return new CreateItemCommand(id, new Barcode(barcode), unitId,
                modelId, model != null ? model.toCommand(unitId, actor) : null, manufacturingYear,
                usageIntensity, serialNumber, acquiredAt, name, condition, hasDamages, damages,
                notes, actor);
    }
}
