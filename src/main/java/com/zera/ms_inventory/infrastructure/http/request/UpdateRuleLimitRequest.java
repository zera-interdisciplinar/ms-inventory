package com.zera.ms_inventory.infrastructure.http.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.zera.ms_inventory.core.domain.valueobject.RuleLimitUnit;

public record UpdateRuleLimitRequest(
        @NotNull @Positive Integer limitValue,
        @NotNull RuleLimitUnit limitUnit
) {}
