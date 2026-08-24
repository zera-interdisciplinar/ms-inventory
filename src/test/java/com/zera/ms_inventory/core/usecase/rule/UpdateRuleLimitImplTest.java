package com.zera.ms_inventory.core.usecase.rule;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.domain.exception.RuleNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.RuleKind;
import com.zera.ms_inventory.core.domain.valueobject.RuleLimitUnit;
import com.zera.ms_inventory.core.domain.valueobject.RuleTargetType;
import com.zera.ms_inventory.core.repository.RuleRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateRuleLimitImplTest {

    @Mock
    private RuleRepository ruleRepository;

    @Test
    void shouldUpdateRuleLimit() {
        UUID id = UUID.randomUUID();
        Rule rule = new Rule(id, "Warranty check", RuleKind.WARRANTY_EXPIRATION, 12, RuleLimitUnit.MONTHS,
                RuleTargetType.MODEL, UUID.randomUUID(), true);
        when(ruleRepository.findById(id)).thenReturn(Optional.of(rule));
        when(ruleRepository.save(rule)).thenReturn(rule);

        UpdateRuleLimitImpl useCase = new UpdateRuleLimitImpl(ruleRepository);
        Rule result = useCase.execute(id, 24, RuleLimitUnit.MONTHS);

        assertEquals(24, result.getLimitValue());
        assertEquals(RuleLimitUnit.MONTHS, result.getLimitUnit());
        verify(ruleRepository).save(rule);
    }

    @Test
    void shouldThrowWhenRuleDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(ruleRepository.findById(id)).thenReturn(Optional.empty());

        UpdateRuleLimitImpl useCase = new UpdateRuleLimitImpl(ruleRepository);

        assertThrows(RuleNotFoundException.class, () -> useCase.execute(id, 24, RuleLimitUnit.MONTHS));
    }
}
