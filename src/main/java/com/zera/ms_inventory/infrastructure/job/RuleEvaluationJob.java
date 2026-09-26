package com.zera.ms_inventory.infrastructure.job;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.repository.JobLock;
import com.zera.ms_inventory.core.usecase.rule.EvaluateRules;

/**
 * Avaliacao periodica das regras. O horario vem da configuracao, com o fuso declarado porque o
 * container roda em UTC e um cron sem zona dispararia tres horas antes do esperado.
 */
@Component
public class RuleEvaluationJob {

    static final String JOB_NAME = "rule-evaluation";
    /** A janela e a hora do disparo: duas execucoes no mesmo dia sao janelas diferentes. */
    private static final DateTimeFormatter WINDOW = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH");

    private static final Logger log = LoggerFactory.getLogger(RuleEvaluationJob.class);

    private final EvaluateRules evaluateRules;
    private final JobLock jobLock;
    private final boolean enabled;

    public RuleEvaluationJob(EvaluateRules evaluateRules, JobLock jobLock,
                             @Value("${zera.rules.evaluation-enabled:true}") boolean enabled) {
        this.evaluateRules = evaluateRules;
        this.jobLock = jobLock;
        this.enabled = enabled;
    }

    // "-" desliga o agendamento: ambiente sem o cron configurado sobe sem job, em vez de
    // falhar na criacao do bean
    @Scheduled(cron = "${zera.rules.evaluation-cron:-}",
            zone = "${zera.rules.evaluation-zone:America/Sao_Paulo}")
    public void run() {
        runAt(LocalDateTime.now());
    }

    /** Separado do gatilho do agendador para o teste conseguir fixar a janela. */
    void runAt(LocalDateTime moment) {
        if (!enabled) {
            log.info("Rule evaluation disabled (zera.rules.evaluation-enabled=false)");
            return;
        }
        String window = moment.format(WINDOW);
        if (!jobLock.acquire(JOB_NAME, window)) {
            log.info("Rule evaluation already running for window {} in another replica", window);
            return;
        }
        evaluateRules.execute(LocalDate.from(moment));
    }
}
