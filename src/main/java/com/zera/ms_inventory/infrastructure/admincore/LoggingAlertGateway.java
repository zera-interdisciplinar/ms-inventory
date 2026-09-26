package com.zera.ms_inventory.infrastructure.admincore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.RuleAlert;
import com.zera.ms_inventory.core.repository.AlertGateway;

/**
 * Usado quando a integracao com o admin-core esta desligada, como em teste e em ambiente sem
 * credencial. Registra o alerta e responde que saiu, para o job seguir o fluxo normal.
 */
@Component
@ConditionalOnMissingBean(AdminCoreAlertGateway.class)
public class LoggingAlertGateway implements AlertGateway {

    private static final Logger log = LoggerFactory.getLogger(LoggingAlertGateway.class);

    @Override
    public boolean send(RuleAlert alert) {
        log.info("[ALERT] unit={} kind={} severity={} subject={} description={}",
                alert.unitId(), alert.kind(), alert.severity(), alert.dedupSubject(), alert.description());
        return true;
    }
}
