package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.domain.valueobject.RuleKind;
import com.zera.ms_inventory.core.domain.valueobject.RuleLimitUnit;
import com.zera.ms_inventory.core.domain.valueobject.RuleTargetType;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.RuleNode;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.RuleMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleRepositoryImplTest {

    @Mock
    private RuleNeo4jRepository neo4jRepository;

    private final RuleMapper mapper = new RuleMapper();

    private RuleRepositoryImpl repository;

    @Test
    void shouldSaveRule() {
        repository = new RuleRepositoryImpl(neo4jRepository, mapper);
        UUID id = UUID.randomUUID();
        Rule rule = new Rule(id, "Warranty check", RuleKind.WARRANTY_EXPIRATION, 12, RuleLimitUnit.MONTHS,
                RuleTargetType.MODEL, UUID.randomUUID(), true);
        when(neo4jRepository.save(any(RuleNode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Rule result = repository.save(rule);

        assertEquals(rule.getId(), result.getId());
        assertEquals(rule.getName(), result.getName());
    }

    @Test
    void shouldFindById() {
        repository = new RuleRepositoryImpl(neo4jRepository, mapper);
        UUID id = UUID.randomUUID();
        RuleNode node = mapper.toNode(new Rule(id, "Warranty check", RuleKind.WARRANTY_EXPIRATION, 12,
                RuleLimitUnit.MONTHS, RuleTargetType.MODEL, UUID.randomUUID(), true));
        when(neo4jRepository.findById(id)).thenReturn(Optional.of(node));

        Optional<Rule> result = repository.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        repository = new RuleRepositoryImpl(neo4jRepository, mapper);
        UUID id = UUID.randomUUID();
        when(neo4jRepository.findById(id)).thenReturn(Optional.empty());

        assertTrue(repository.findById(id).isEmpty());
    }

    @Test
    void shouldFindAll() {
        repository = new RuleRepositoryImpl(neo4jRepository, mapper);
        RuleNode node = mapper.toNode(new Rule(UUID.randomUUID(), "Warranty check", RuleKind.WARRANTY_EXPIRATION, 12,
                RuleLimitUnit.MONTHS, RuleTargetType.MODEL, UUID.randomUUID(), true));
        when(neo4jRepository.findAll()).thenReturn(List.of(node));

        List<Rule> result = repository.findAll();

        assertEquals(1, result.size());
        assertEquals("Warranty check", result.get(0).getName());
    }

    @Test
    void shouldDeleteById() {
        repository = new RuleRepositoryImpl(neo4jRepository, mapper);
        UUID id = UUID.randomUUID();

        repository.deleteById(id);

        verify(neo4jRepository).deleteById(id);
    }
}
