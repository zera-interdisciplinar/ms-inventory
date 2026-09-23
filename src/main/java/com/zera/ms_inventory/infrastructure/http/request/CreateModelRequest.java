package com.zera.ms_inventory.infrastructure.http.request;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.MaterialCode;
import com.zera.ms_inventory.core.usecase.model.CreateModelCommand;

/** Campos do cadastro de modelo do app: nome, material, marca e observacao; o resto e opcional. */
public record CreateModelRequest(
        @NotBlank String name,
        @NotBlank String manufacturer,
        @Positive Integer warrantyMonths,
        @Positive Integer expectedLifespanMonths,
        @NotEmpty Set<MaterialCode> materials,
        @Positive Double estimatedWeightKg,
        @Size(max = 500) String notes,
        @NotNull UUID categoryId
) {
    /** unitId vem do header X-Unit-Id e o autor do token, nunca do corpo. */
    public CreateModelCommand toCommand(UUID unitId, Actor actor) {
        return new CreateModelCommand(unitId, name, manufacturer, warrantyMonths, expectedLifespanMonths,
                materials, estimatedWeightKg, notes, categoryId, actor);
    }
}
