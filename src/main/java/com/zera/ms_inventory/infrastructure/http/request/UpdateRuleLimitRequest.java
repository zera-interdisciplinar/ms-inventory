package com.zera.ms_inventory.infrastructure.http.request;

import jakarta.validation.constraints.PositiveOrZero;

import com.zera.ms_inventory.core.domain.valueobject.RuleLimitUnit;

/** Limite opcional: há regra que só liga e desliga, sem número. */
public record UpdateRuleLimitRequest(@PositiveOrZero Integer limitValue, RuleLimitUnit limitUnit) {
}
