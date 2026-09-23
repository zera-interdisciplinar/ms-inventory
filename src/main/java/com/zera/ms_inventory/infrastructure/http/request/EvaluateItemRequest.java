package com.zera.ms_inventory.infrastructure.http.request;

import java.util.Set;

import jakarta.validation.constraints.NotNull;

import com.zera.ms_inventory.core.domain.valueobject.DamageType;
import com.zera.ms_inventory.core.domain.valueobject.ItemCondition;

/**
 * Avaliação do item que voltou da manutenção. Os danos são opcionais: sem eles, o que já estava
 * registrado continua valendo.
 */
public record EvaluateItemRequest(
        @NotNull ItemCondition condition,
        Boolean hasDamages,
        Set<DamageType> damages
) {
}
