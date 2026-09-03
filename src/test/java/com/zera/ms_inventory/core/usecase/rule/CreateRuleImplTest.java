package com.zera.ms_inventory.core.usecase.rule;

import java.time.LocalDateTime;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateRuleImplTest {

    @Mock
    private RuleRepository ruleRepository;

    @Test
    void shouldCreateRule() {
        when(ruleRepository.save(any(Rule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateRuleImpl useCase = new CreateRuleImpl(ruleRepository);
        LocalDateTime now = LocalDateTime.now();
        UUID targetId = UUID.randomUUID();
        Rule result = useCase.execute("Warranty check", RuleKind.WARRANTY_EXPIRATION, 12, RuleLimitUnit.MONTHS,
                RuleTargetType.MODEL, targetId, true, now, now);

        assertNotNull(result.getId());
        assertEquals("Warranty check", result.getName());
        assertEquals(RuleKind.WARRANTY_EXPIRATION, result.getKind());
        assertEquals(targetId, result.getTargetId());
        verify(ruleRepository).save(result);
    }
}
