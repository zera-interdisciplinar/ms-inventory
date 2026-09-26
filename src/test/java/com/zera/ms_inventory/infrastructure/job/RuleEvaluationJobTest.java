package com.zera.ms_inventory.infrastructure.job;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.core.repository.JobLock;
import com.zera.ms_inventory.core.usecase.rule.EvaluateRules;
import com.zera.ms_inventory.core.usecase.rule.RuleEvaluationResult;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleEvaluationJobTest {

    @Mock private EvaluateRules evaluateRules;
    @Mock private JobLock jobLock;

    private RuleEvaluationJob job(boolean habilitado) {
        return new RuleEvaluationJob(evaluateRules, jobLock, habilitado);
    }

    @Test
    void shouldEvaluateWhenItWinsTheLock() {
        LocalDateTime momento = LocalDateTime.of(2026, 9, 26, 3, 0);
        when(jobLock.acquire(RuleEvaluationJob.JOB_NAME, "2026-09-26T03")).thenReturn(true);
        when(evaluateRules.execute(any())).thenReturn(RuleEvaluationResult.empty());

        job(true).runAt(momento);

        verify(evaluateRules).execute(LocalDate.of(2026, 9, 26));
    }

    /** Segunda replica na mesma janela nao roda: os alertas sairiam em dobro. */
    @Test
    void shouldSkipWhenAnotherReplicaHasTheWindow() {
        when(jobLock.acquire(any(), any())).thenReturn(false);

        job(true).runAt(LocalDateTime.of(2026, 9, 26, 3, 0));

        verify(evaluateRules, never()).execute(any());
    }

    /** Duas execucoes no mesmo dia sao janelas distintas, entao ambas rodam. */
    @Test
    void shouldTreatEachHourAsItsOwnWindow() {
        when(jobLock.acquire(eq(RuleEvaluationJob.JOB_NAME), any())).thenReturn(true);
        when(evaluateRules.execute(any())).thenReturn(RuleEvaluationResult.empty());

        job(true).runAt(LocalDateTime.of(2026, 9, 26, 3, 0));
        job(true).runAt(LocalDateTime.of(2026, 9, 26, 15, 0));

        verify(jobLock).acquire(RuleEvaluationJob.JOB_NAME, "2026-09-26T03");
        verify(jobLock).acquire(RuleEvaluationJob.JOB_NAME, "2026-09-26T15");
        verify(evaluateRules, org.mockito.Mockito.times(2)).execute(LocalDate.of(2026, 9, 26));
    }

    @Test
    void shouldNotEvenTakeTheLockWhenDisabled() {
        job(false).runAt(LocalDateTime.now());

        verify(jobLock, never()).acquire(any(), any());
        verify(evaluateRules, never()).execute(any());
    }
}
