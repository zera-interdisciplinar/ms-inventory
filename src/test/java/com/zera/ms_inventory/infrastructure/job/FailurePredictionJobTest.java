package com.zera.ms_inventory.infrastructure.job;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.core.repository.JobLock;
import com.zera.ms_inventory.core.usecase.prediction.UpdateFailurePredictions;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FailurePredictionJobTest {

    private static final LocalDateTime MOMENT = LocalDateTime.of(2026, 9, 26, 2, 0);

    @Mock
    private UpdateFailurePredictions updateFailurePredictions;

    @Mock
    private JobLock jobLock;

    @Test
    void shouldRunWhenItTakesTheWindow() {
        when(jobLock.acquire("failure-prediction", "2026-09-26T02")).thenReturn(true);

        new FailurePredictionJob(updateFailurePredictions, jobLock, true).runAt(MOMENT);

        verify(updateFailurePredictions).execute();
    }

    /** Duas replicas dispararam: a segunda desiste em vez de prever tudo de novo. */
    @Test
    void shouldStandDownWhenAnotherReplicaHasTheWindow() {
        when(jobLock.acquire(eq("failure-prediction"), eq("2026-09-26T02"))).thenReturn(false);

        new FailurePredictionJob(updateFailurePredictions, jobLock, true).runAt(MOMENT);

        verify(updateFailurePredictions, never()).execute();
    }

    @Test
    void shouldNotEvenTakeTheLockWhenDisabled() {
        new FailurePredictionJob(updateFailurePredictions, jobLock, false).runAt(MOMENT);

        verifyNoInteractions(jobLock);
        verifyNoInteractions(updateFailurePredictions);
    }

    /** A janela e a hora do disparo, igual ao job de regras. */
    @Test
    void shouldKeyTheWindowByTheHourOfTheRun() {
        when(jobLock.acquire("failure-prediction", "2026-12-31T23")).thenReturn(true);

        new FailurePredictionJob(updateFailurePredictions, jobLock, true)
                .runAt(LocalDateTime.of(2026, 12, 31, 23, 59, 59));

        verify(jobLock).acquire("failure-prediction", "2026-12-31T23");
    }
}
