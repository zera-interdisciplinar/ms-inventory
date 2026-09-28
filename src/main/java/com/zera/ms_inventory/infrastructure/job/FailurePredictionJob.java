package com.zera.ms_inventory.infrastructure.job;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.repository.JobLock;
import com.zera.ms_inventory.core.usecase.prediction.UpdateFailurePredictions;

/**
 * Atualizacao diaria da previsao de quebra. Roda as 02:00, antes da avaliacao de regras das 03:00,
 * para o alerta de quebra prevista do mesmo dia ja olhar a previsao nova em vez da de ontem.
 */
@Component
public class FailurePredictionJob {

    static final String JOB_NAME = "failure-prediction";
    /** Mesma trava do job de regras: a janela e a hora, e a segunda replica desiste dela. */
    private static final DateTimeFormatter WINDOW = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH");

    private static final Logger log = LoggerFactory.getLogger(FailurePredictionJob.class);

    private final UpdateFailurePredictions updateFailurePredictions;
    private final JobLock jobLock;
    private final boolean enabled;

    public FailurePredictionJob(UpdateFailurePredictions updateFailurePredictions, JobLock jobLock,
                                @Value("${zera.prediction.job-enabled:true}") boolean enabled) {
        this.updateFailurePredictions = updateFailurePredictions;
        this.jobLock = jobLock;
        this.enabled = enabled;
    }

    // "-" desliga o agendamento, como no job de regras
    @Scheduled(cron = "${zera.prediction.cron:-}",
            zone = "${zera.prediction.zone:America/Sao_Paulo}")
    public void run() {
        runAt(LocalDateTime.now());
    }

    /** Separado do gatilho do agendador para o teste conseguir fixar a janela. */
    void runAt(LocalDateTime moment) {
        if (!enabled) {
            log.info("Failure prediction disabled (zera.prediction.job-enabled=false)");
            return;
        }
        String window = moment.format(WINDOW);
        if (!jobLock.acquire(JOB_NAME, window)) {
            log.info("Failure prediction already running for window {} in another replica", window);
            return;
        }
        updateFailurePredictions.execute();
    }
}
