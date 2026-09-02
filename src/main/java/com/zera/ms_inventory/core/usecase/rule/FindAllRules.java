package com.zera.ms_inventory.core.usecase.rule;

import java.util.List;

import com.zera.ms_inventory.core.domain.entity.Rule;

public interface FindAllRules {
    List<Rule> execute();
}
