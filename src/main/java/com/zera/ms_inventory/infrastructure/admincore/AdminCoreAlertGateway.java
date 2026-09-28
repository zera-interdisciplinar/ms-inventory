package com.zera.ms_inventory.infrastructure.admincore;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.RuleAlert;
import com.zera.ms_inventory.core.repository.AlertGateway;

/**
 * Entrega o alerta ao admin-core, tentando de novo com espera crescente. Sem gestor na unidade o
 * alerta nao tem destinatario: e registrado e descartado, sem derrubar a execucao.
 */
@Component
@ConditionalOnProperty(prefix = "zera.admin-core", name = "enabled", havingValue = "true")
public class AdminCoreAlertGateway implements AlertGateway {

    private static final Logger log = LoggerFactory.getLogger(AdminCoreAlertGateway.class);

    private final AdminCoreClient client;
    private final AdminCoreProperties properties;

    public AdminCoreAlertGateway(AdminCoreClient client, AdminCoreProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public boolean send(RuleAlert alert) {
        if (!properties.isConfigured()) {
            log.debug("admin-core not configured; alert {} not sent", alert.kind());
            return false;
        }
        Optional<UUID> recipient = client.managerOf(alert.unitId());
        if (recipient.isEmpty()) {
            return false;
        }
        return withRetry(() -> client.postAlert(AdminCoreClient.alertPayload(
                recipient.get(), alert.unitId(), alert.ruleId(), alert.dedupSubject(),
                alert.kind().name(), alert.severity().name(), alert.description(), alert.occurredAt())));
    }

    /** Espera crescente entre as tentativas; a ultima falha so vira log. */
    private boolean withRetry(java.util.function.BooleanSupplier attempt) {
        for (int tentativa = 1; tentativa <= properties.maxAttempts(); tentativa++) {
            if (attempt.getAsBoolean()) {
                return true;
            }
            if (tentativa < properties.maxAttempts()) {
                sleep(properties.retryBackoff().toMillis() * tentativa);
            }
        }
        return false;
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
