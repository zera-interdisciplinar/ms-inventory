package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.neo4j.core.Neo4jClient;

import com.zera.ms_inventory.Fixtures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Trava de execucao e registro de envio, os dois escritos com Cypher direto. */
@ExtendWith(MockitoExtension.class)
class AlertPersistenceTest {

    @Mock private Neo4jClient neo4jClient;
    @Mock private Neo4jClient.UnboundRunnableSpec spec;

    @SuppressWarnings("unchecked")
    private <T> void stubFetchAs(Class<T> tipo, Optional<T> resultado) {
        Neo4jClient.MappingSpec<T> mapping = mock(Neo4jClient.MappingSpec.class);
        Neo4jClient.RecordFetchSpec<T> fetch = mock(Neo4jClient.RecordFetchSpec.class);
        when(neo4jClient.query(anyString())).thenReturn(spec);
        when(spec.bindAll(anyMap())).thenReturn(spec);
        when(spec.fetchAs(tipo)).thenReturn(mapping);
        when(mapping.mappedBy(any())).thenReturn(fetch);
        when(fetch.one()).thenReturn(resultado);
    }

    // ---- trava do job ----

    @Test
    void shouldWinTheWindowWhenTheNodeIsCreated() {
        stubFetchAs(String.class, Optional.of("rule-evaluation:2026-09-26T03"));

        assertThat(new Neo4jJobLock(neo4jClient).acquire("rule-evaluation", "2026-09-26T03")).isTrue();

        ArgumentCaptor<Map<String, Object>> parametros = ArgumentCaptor.forClass(Map.class);
        verify(spec).bindAll(parametros.capture());
        assertThat(parametros.getValue()).containsEntry("id", "rule-evaluation:2026-09-26T03");
    }

    /** A segunda replica esbarra na constraint e desiste, em vez de rodar o job em dobro. */
    @Test
    void shouldLoseTheWindowWhenTheNodeAlreadyExists() {
        when(neo4jClient.query(anyString())).thenReturn(spec);
        when(spec.bindAll(anyMap())).thenReturn(spec);
        when(spec.fetchAs(String.class)).thenThrow(new DataIntegrityViolationException("already exists"));

        assertThat(new Neo4jJobLock(neo4jClient).acquire("rule-evaluation", "2026-09-26T03")).isFalse();
    }

    // ---- registro de envio ----

    @Test
    void shouldBuildOneIdPerUnitRuleAndSubject() {
        UUID ruleId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();

        String id = Neo4jAlertDispatchLog.idOf(Fixtures.UNIT, ruleId, subjectId);

        assertThat(id).isEqualTo(Fixtures.UNIT + ":" + ruleId + ":" + subjectId);
        assertThat(Neo4jAlertDispatchLog.idOf(Fixtures.UNIT, ruleId, subjectId)).isEqualTo(id);
        assertThat(Neo4jAlertDispatchLog.idOf(Fixtures.OTHER_UNIT, ruleId, subjectId)).isNotEqualTo(id);
    }

    @Test
    void shouldAnswerWhetherItWasSentInsideTheWindow() {
        stubFetchAs(Long.class, Optional.of(1L));

        assertThat(new Neo4jAlertDispatchLog(neo4jClient).sentSince(Fixtures.UNIT, UUID.randomUUID(),
                UUID.randomUUID(), LocalDateTime.now().minusHours(24))).isTrue();
    }

    @Test
    void shouldAnswerFalseWithoutAPreviousSend() {
        stubFetchAs(Long.class, Optional.empty());

        assertThat(new Neo4jAlertDispatchLog(neo4jClient).sentSince(Fixtures.UNIT, UUID.randomUUID(),
                UUID.randomUUID(), LocalDateTime.now().minusHours(24))).isFalse();
    }

    @Test
    void shouldRecordTheSendWithItsMoment() {
        UUID ruleId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        LocalDateTime quando = LocalDateTime.of(2026, 9, 26, 3, 5);
        when(neo4jClient.query(anyString())).thenReturn(spec);
        when(spec.bindAll(anyMap())).thenReturn(spec);

        new Neo4jAlertDispatchLog(neo4jClient).recordSent(Fixtures.UNIT, ruleId, subjectId, quando);

        ArgumentCaptor<Map<String, Object>> parametros = ArgumentCaptor.forClass(Map.class);
        verify(spec).bindAll(parametros.capture());
        assertThat(parametros.getValue())
                .containsEntry("id", Neo4jAlertDispatchLog.idOf(Fixtures.UNIT, ruleId, subjectId))
                .containsEntry("sentAt", quando);
        verify(spec).run();
    }
}
