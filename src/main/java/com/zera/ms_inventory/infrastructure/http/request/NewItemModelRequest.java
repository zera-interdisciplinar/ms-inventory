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

/** Modelo criado junto no cadastro manual do item: material e marca do formulario. */
public record NewItemModelRequest(
        @NotBlank String name,
        @NotBlank String manufacturer,
        @NotEmpty Set<MaterialCode> materials,
        @Positive Double estimatedWeightKg,
        @Size(max = 500) String notes,
        @NotNull UUID categoryId
) {
    public CreateModelCommand toCommand(UUID unitId, Actor actor) {
        return new CreateModelCommand(unitId, name, manufacturer, null, null, materials, estimatedWeightKg, notes,
                categoryId, actor);
    }
}
