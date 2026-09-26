package com.zera.ms_inventory.core.usecase.rule;

/** Resumo de uma execucao do job, usado no log e nos testes. */
public record RuleEvaluationResult(int unitsVisited, int rulesEvaluated, int alertsRaised, int alertsSent,
                                   int alertsSuppressed, int alertsFailed) {

    public static RuleEvaluationResult empty() {
        return new RuleEvaluationResult(0, 0, 0, 0, 0, 0);
    }
}
