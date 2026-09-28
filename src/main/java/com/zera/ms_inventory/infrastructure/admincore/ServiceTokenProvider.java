package com.zera.ms_inventory.infrastructure.admincore;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Guarda o token de servico entre chamadas e o renova pouco antes de expirar. Sem isso o job
 * pediria um token novo a cada alerta.
 */
@Component
@ConditionalOnProperty(prefix = "zera.admin-core", name = "enabled", havingValue = "true")
public class ServiceTokenProvider {

    /** Renova um pouco antes de expirar, para nao usar um token que vence no meio da chamada. */
    private static final Duration SAFETY_MARGIN = Duration.ofSeconds(30);

    private final RestClient restClient;
    private final AdminCoreProperties properties;

    private volatile String token;
    private volatile Instant expiresAt = Instant.EPOCH;

    @org.springframework.beans.factory.annotation.Autowired
    public ServiceTokenProvider(AdminCoreProperties properties) {
        this(AdminCoreRestClient.of(properties), properties);
    }

    /** Recebe o cliente pronto; o teste usa este caminho para falar com um servidor simulado. */
    ServiceTokenProvider(RestClient restClient, AdminCoreProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public synchronized String token() {
        if (token != null && Instant.now().isBefore(expiresAt.minus(SAFETY_MARGIN))) {
            return token;
        }
        Map<String, Object> response = restClient.post()
                .uri("/api/v1/auth/service-token")
                .body(Map.of("clientId", properties.clientId(), "clientSecret", properties.clientSecret()))
                .retrieve()
                .body(Map.class);
        if (response == null || response.get("accessToken") == null) {
            throw new IllegalStateException("admin-core did not return a service token");
        }
        token = (String) response.get("accessToken");
        long expiresIn = response.get("expiresIn") instanceof Number seconds ? seconds.longValue() : 0L;
        expiresAt = Instant.now().plusSeconds(expiresIn);
        return token;
    }

    /** Descarta o token guardado; usado quando o admin-core responde 401. */
    public synchronized void invalidate() {
        token = null;
        expiresAt = Instant.EPOCH;
    }
}
