package com.zera.ms_inventory.core.usecase.rule;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.RuleAlert;
import com.zera.ms_inventory.core.domain.valueobject.AlertSeverity;
import com.zera.ms_inventory.core.domain.valueobject.AlertSubject;
import com.zera.ms_inventory.core.domain.valueobject.RuleKind;
import com.zera.ms_inventory.core.repository.AlertDispatchLog;
import com.zera.ms_inventory.core.repository.AlertGateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertDispatcherTest {

    @Mock private AlertGateway gateway;
    @Mock private AlertDispatchLog dispatchLog;

    private final UUID ruleId = UUID.randomUUID();
    private final UUID itemId = UUID.randomUUID();

    private AlertDispatcher dispatcher(Duration janela) {
        return new AlertDispatcher(gateway, dispatchLog, janela);
    }

    private RuleAlert alerta() {
        return new RuleAlert(Fixtures.UNIT, ruleId, RuleKind.STALE_ITEM,
                new AlertSubject(itemId, "100001", "Notebook"), AlertSeverity.LOW, "parado", null);
    }

    @Test
    void shouldSendAndRecordWhenOutsideTheWindow() {
        when(dispatchLog.sentSince(any(), any(), any(), any())).thenReturn(false);
        when(gateway.send(any())).thenReturn(true);

        assertThat(dispatcher(Duration.ofHours(24)).dispatch(alerta()))
                .isEqualTo(AlertDispatcher.Outcome.SENT);

        verify(gateway).send(any());
        verify(dispatchLog).recordSent(org.mockito.ArgumentMatchers.eq(Fixtures.UNIT),
                org.mockito.ArgumentMatchers.eq(ruleId), org.mockito.ArgumentMatchers.eq(itemId), any());
    }

    /**
     * O mesmo item continua fora do limite a cada execucao; sem a janela, a pessoa receberia o
     * mesmo aviso em toda rodada do job.
     */
    @Test
    void shouldSuppressInsideTheWindowWithoutCallingTheGateway() {
        when(dispatchLog.sentSince(any(), any(), any(), any())).thenReturn(true);

        assertThat(dispatcher(Duration.ofHours(24)).dispatch(alerta()))
                .isEqualTo(AlertDispatcher.Outcome.SUPPRESSED);

        verify(gateway, never()).send(any());
        verify(dispatchLog, never()).recordSent(any(), any(), any(), any());
    }

    /** Falha nao e registrada: a proxima execucao precisa tentar de novo. */
    @Test
    void shouldNotRecordAFailedDelivery() {
        when(dispatchLog.sentSince(any(), any(), any(), any())).thenReturn(false);
        when(gateway.send(any())).thenReturn(false);

        assertThat(dispatcher(Duration.ofHours(24)).dispatch(alerta()))
                .isEqualTo(AlertDispatcher.Outcome.FAILED);

        verify(dispatchLog, never()).recordSent(any(), any(), any(), any());
    }

    @Test
    void shouldUseTheConfiguredWindowAsTheCutoff() {
        when(dispatchLog.sentSince(any(), any(), any(), any())).thenReturn(true);

        dispatcher(Duration.ofHours(6)).dispatch(alerta());

        ArgumentCaptor<LocalDateTime> desde = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(dispatchLog).sentSince(org.mockito.ArgumentMatchers.eq(Fixtures.UNIT),
                org.mockito.ArgumentMatchers.eq(ruleId), org.mockito.ArgumentMatchers.eq(itemId),
                desde.capture());
        assertThat(desde.getValue()).isBefore(LocalDateTime.now().minusHours(5))
                .isAfter(LocalDateTime.now().minusHours(7));
    }

    /** Alerta de unidade inteira deduplica pela unidade, ja que nao ha item. */
    @Test
    void shouldDeduplicateAUnitWideAlertByTheUnit() {
        when(dispatchLog.sentSince(any(), any(), any(), any())).thenReturn(false);
        when(gateway.send(any())).thenReturn(true);
        RuleAlert daUnidade = new RuleAlert(Fixtures.UNIT, ruleId, RuleKind.STOCK_QUANTITY_LIMIT,
                AlertSubject.unit(), AlertSeverity.HIGH, "cheio", null);

        dispatcher(Duration.ofHours(24)).dispatch(daUnidade);

        verify(dispatchLog).recordSent(org.mockito.ArgumentMatchers.eq(Fixtures.UNIT),
                org.mockito.ArgumentMatchers.eq(ruleId),
                org.mockito.ArgumentMatchers.eq(Fixtures.UNIT), any());
    }
}
