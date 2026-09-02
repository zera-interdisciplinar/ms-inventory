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
class UpdateRuleTargetImplTest {

    @Mock
    private RuleRepository ruleRepository;

    @Test
    void shouldUpdateRuleTarget() {
        UUID id = UUID.randomUUID();
        UUID newTargetId = UUID.randomUUID();
        Rule rule = new Rule(id, "Warranty check", RuleKind.WARRANTY_EXPIRATION, 12, RuleLimitUnit.MONTHS,
                RuleTargetType.MODEL, UUID.randomUUID(), true);
        when(ruleRepository.findById(id)).thenReturn(Optional.of(rule));
        when(ruleRepository.save(rule)).thenReturn(rule);

        UpdateRuleTargetImpl useCase = new UpdateRuleTargetImpl(ruleRepository);
        Rule result = useCase.execute(id, RuleTargetType.CATEGORY, newTargetId);

        assertEquals(RuleTargetType.CATEGORY, result.getTargetType());
        assertEquals(newTargetId, result.getTargetId());
        verify(ruleRepository).save(rule);
    }

    @Test
    void shouldThrowWhenRuleDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(ruleRepository.findById(id)).thenReturn(Optional.empty());

        UpdateRuleTargetImpl useCase = new UpdateRuleTargetImpl(ruleRepository);

        assertThrows(RuleNotFoundException.class,
                () -> useCase.execute(id, RuleTargetType.CATEGORY, UUID.randomUUID()));
    }
}
