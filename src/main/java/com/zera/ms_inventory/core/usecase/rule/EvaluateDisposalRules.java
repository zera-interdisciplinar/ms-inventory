package com.zera.ms_inventory.core.usecase.rule;

import com.zera.ms_inventory.core.domain.entity.Disposal;

public interface EvaluateDisposalRules {
    /** Avalia no momento do descarte; avisar no dia seguinte chegaria tarde para reverter. */
    void execute(Disposal disposal);
}
