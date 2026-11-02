package com.zera.ms_inventory.infrastructure.admincore;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.RuleAlert;
import com.zera.ms_inventory.core.repository.AlertGateway;

/**
 * Entrega o alerta ao admin-core, tentando de novo com espera crescente, a todos os gestores da
 * unidade -- o alerta e da unidade, nao de um gestor. Sem gestor na unidade o alerta nao tem
 * destinatario: e registrado e descartado, sem derrubar a execucao.
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
        List<UUID> recipients = client.managersOf(alert.unitId());
        if (recipients.isEmpty()) {
            return false;
        }
        String deepLink = alert.subject().isUnitWide()
                ? null
                : AdminCoreClient.itemDeepLink(alert.subject().itemId());
        // todos os envios sao tentados mesmo se um falhar, senao um gestor com problema de entrega
        // deixaria os seguintes sem alerta. Reenviar ao gestor que ja recebeu e inofensivo: o
        // admin-core deduplica por (regra, evento, usuario) enquanto o alerta esta OPEN.
        boolean allDelivered = true;
        for (UUID recipient : recipients) {
            boolean delivered = withRetry(() -> client.postAlert(AdminCoreClient.alertPayload(
                    recipient, alert.unitId(), alert.ruleId(), alert.dedupSubject(),
                    alert.kind().name(), alert.severity().name(), alert.description(), alert.occurredAt(),
                    deepLink)));
            allDelivered &= delivered;
        }
        return allDelivered;
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
