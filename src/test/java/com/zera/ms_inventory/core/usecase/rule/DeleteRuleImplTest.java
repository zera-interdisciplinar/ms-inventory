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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteRuleImplTest {

    @Mock
    private RuleRepository ruleRepository;

    @Test
    void shouldDeleteRuleWhenItExists() {
        UUID id = UUID.randomUUID();
        Rule rule = new Rule(id, "Warranty check", RuleKind.WARRANTY_EXPIRATION, 12, RuleLimitUnit.MONTHS,
                RuleTargetType.MODEL, UUID.randomUUID(), true);
        when(ruleRepository.findById(id)).thenReturn(Optional.of(rule));

        DeleteRuleImpl useCase = new DeleteRuleImpl(ruleRepository);
        useCase.execute(id);

        verify(ruleRepository).deleteById(id);
    }

    @Test
    void shouldThrowWhenRuleDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(ruleRepository.findById(id)).thenReturn(Optional.empty());

        DeleteRuleImpl useCase = new DeleteRuleImpl(ruleRepository);

        assertThrows(RuleNotFoundException.class, () -> useCase.execute(id));
    }
}
