package com.zera.ms_inventory.core.usecase.rule;

import java.time.Duration;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.RuleAlert;
import com.zera.ms_inventory.core.repository.AlertDispatchLog;
import com.zera.ms_inventory.core.repository.AlertGateway;

/**
 * Decide se o alerta vai para fora. Dentro da janela, o mesmo par (regra, assunto) nao e reenviado:
 * um item que continua fora do limite geraria uma chamada por execucao do job, e a pessoa receberia
 * o mesmo aviso todo dia.
 */
@Service
public class AlertDispatcher {

    private static final Logger log = LoggerFactory.getLogger(AlertDispatcher.class);

    private final AlertGateway gateway;
    private final AlertDispatchLog dispatchLog;
    private final Duration window;

    public AlertDispatcher(AlertGateway gateway, AlertDispatchLog dispatchLog,
                           @Value("${zera.alerts.dedup-window:PT24H}") Duration window) {
        this.gateway = gateway;
        this.dispatchLog = dispatchLog;
        this.window = window;
    }

    public enum Outcome { SENT, SUPPRESSED, FAILED }

    public Outcome dispatch(RuleAlert alert) {
        LocalDateTime since = LocalDateTime.now().minus(window);
        if (dispatchLog.sentSince(alert.unitId(), alert.ruleId(), alert.dedupSubject(), since)) {
            return Outcome.SUPPRESSED;
        }
        if (!gateway.send(alert)) {
            // nao registra: a proxima execucao tenta de novo
            log.warn("Alert not delivered; will retry next run. rule={} subject={}",
                    alert.ruleId(), alert.dedupSubject());
            return Outcome.FAILED;
        }
        dispatchLog.recordSent(alert.unitId(), alert.ruleId(), alert.dedupSubject(), LocalDateTime.now());
        return Outcome.SENT;
    }
}
