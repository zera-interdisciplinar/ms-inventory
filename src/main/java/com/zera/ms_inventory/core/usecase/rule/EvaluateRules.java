package com.zera.ms_inventory.core.usecase.rule;

import java.time.LocalDate;

public interface EvaluateRules {
    /** Avalia todas as unidades com regras configuradas. */
    RuleEvaluationResult execute(LocalDate reference);
}
