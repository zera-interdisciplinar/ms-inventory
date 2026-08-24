package com.zera.ms_inventory.infrastructure.http.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

import com.zera.ms_inventory.core.domain.valueobject.RuleTargetType;

public record UpdateRuleTargetRequest(
        @NotNull RuleTargetType targetType,
        @NotNull UUID targetId
) {}
