package com.zera.ms_inventory.infrastructure.admincore;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Conversa com o admin-core: resolve o gestor da unidade e entrega o alerta. As falhas viram
 * {@code false} em vez de excecao, porque nenhum alerta pode derrubar o job nem o descarte.
 */
@Component
@ConditionalOnProperty(prefix = "zera.admin-core", name = "enabled", havingValue = "true")
public class AdminCoreClient {

    private static final Logger log = LoggerFactory.getLogger(AdminCoreClient.class);

    private final RestClient restClient;
    private final ServiceTokenProvider tokens;
    private final AdminCoreProperties properties;
    private final Map<UUID, CachedRecipient> recipients = new java.util.concurrent.ConcurrentHashMap<>();

    private record CachedRecipient(UUID userId, Instant expiresAt) {}

    @org.springframework.beans.factory.annotation.Autowired
    public AdminCoreClient(ServiceTokenProvider tokens, AdminCoreProperties properties) {
        this(AdminCoreRestClient.of(properties), tokens, properties);
    }

    /** Recebe o cliente pronto; o teste usa este caminho para falar com um servidor simulado. */
    AdminCoreClient(RestClient restClient, ServiceTokenProvider tokens, AdminCoreProperties properties) {
        this.restClient = restClient;
        this.tokens = tokens;
        this.properties = properties;
    }

    /**
     * Gestor da unidade, que e quem recebe os alertas de inventario. O resultado fica em cache
     * curto: sem isso, cada alerta de uma mesma execucao repetiria a consulta.
     */
    public Optional<UUID> managerOf(UUID unitId) {
        CachedRecipient cached = recipients.get(unitId);
        if (cached != null && Instant.now().isBefore(cached.expiresAt())) {
            return Optional.of(cached.userId());
        }
        try {
            List<Map<String, Object>> users = restClient.get()
                    .uri(uri -> uri.path("/api/v1/users")
                            .queryParam("role", "MANAGER")
                            .queryParam("unitId", unitId)
                            .queryParam("size", 1)
                            .build())
                    .header("Authorization", "Bearer " + tokens.token())
                    .retrieve()
                    .body(List.class);
            if (users == null || users.isEmpty()) {
                log.warn("No manager found for unit {}; alerts have no recipient", unitId);
                return Optional.empty();
            }
            UUID userId = UUID.fromString((String) users.get(0).get("userId"));
            recipients.put(unitId, new CachedRecipient(userId,
                    Instant.now().plus(properties.recipientCacheTtl())));
            return Optional.of(userId);
        } catch (RuntimeException failure) {
            invalidateOnUnauthorized(failure);
            log.warn("Could not resolve the manager of unit {}: {}", unitId, failure.getMessage());
            return Optional.empty();
        }
    }

    /** Uma tentativa de entrega; quem decide repetir e o chamador. */
    public boolean postAlert(Map<String, Object> payload) {
        try {
            restClient.post()
                    .uri("/api/v1/notifications/alerts")
                    .header("Authorization", "Bearer " + tokens.token())
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RuntimeException failure) {
            invalidateOnUnauthorized(failure);
            log.warn("Alert delivery failed: {}", failure.getMessage());
            return false;
        }
    }

    /** Token vencido antes da hora prevista: descarta para a proxima tentativa pedir outro. */
    private void invalidateOnUnauthorized(RuntimeException failure) {
        if (failure instanceof RestClientResponseException response) {
            HttpStatusCode status = response.getStatusCode();
            if (status.value() == 401 || status.value() == 403) {
                tokens.invalidate();
            }
        }
    }

    public static Map<String, Object> alertPayload(UUID userId, UUID unitId, UUID ruleId, UUID eventId,
                                                   String kind, String severity, String description,
                                                   java.time.LocalDateTime occurredAt) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId.toString());
        payload.put("unitId", unitId.toString());
        // o par (ruleId, eventId) e o que o admin-core usa para reconhecer o alerta repetido
        payload.put("ruleId", ruleId == null ? null : ruleId.toString());
        payload.put("eventId", eventId == null ? null : eventId.toString());
        payload.put("kind", kind);
        payload.put("severity", severity);
        payload.put("status", "OPEN");
        payload.put("description", description);
        payload.put("occurredAt", occurredAt.toString());
        return payload;
    }
}
