package com.zera.ms_inventory.core.usecase.rule;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.domain.valueobject.RuleKind;
import com.zera.ms_inventory.core.domain.valueobject.RuleLimitUnit;
import com.zera.ms_inventory.core.domain.valueobject.RuleTargetType;
import com.zera.ms_inventory.core.repository.RuleRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindAllRulesImplTest {

    @Mock
    private RuleRepository ruleRepository;

    @Test
    void shouldReturnAllRules() {
        Rule rule = new Rule(UUID.randomUUID(), "Warranty check", RuleKind.WARRANTY_EXPIRATION, 12,
                RuleLimitUnit.MONTHS, RuleTargetType.MODEL, UUID.randomUUID(), true);
        when(ruleRepository.findAll()).thenReturn(List.of(rule));

        FindAllRulesImpl useCase = new FindAllRulesImpl(ruleRepository);

        assertEquals(List.of(rule), useCase.execute());
    }
}
