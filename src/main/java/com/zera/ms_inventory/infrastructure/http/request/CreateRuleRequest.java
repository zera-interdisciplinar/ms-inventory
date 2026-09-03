package com.zera.ms_inventory.infrastructure.http.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.zera.ms_inventory.core.domain.valueobject.RuleKind;
import com.zera.ms_inventory.core.domain.valueobject.RuleLimitUnit;
import com.zera.ms_inventory.core.domain.valueobject.RuleTargetType;

public record CreateRuleRequest(
        @NotBlank String name,
        @NotNull RuleKind kind,
        @NotNull @Positive Integer limitValue,
        @NotNull RuleLimitUnit limitUnit,
        @NotNull RuleTargetType targetType,
        @NotNull UUID targetId,
        boolean active
) {}
