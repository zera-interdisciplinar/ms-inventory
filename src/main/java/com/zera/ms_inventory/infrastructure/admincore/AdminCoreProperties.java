package com.zera.ms_inventory.infrastructure.admincore;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Integracao com o admin-core. Desligada quando {@code enabled} e falso ou o segredo esta em
 * branco: ambiente sem credencial nao deve tentar autenticar a cada alerta.
 */
@ConfigurationProperties(prefix = "zera.admin-core")
public record AdminCoreProperties(
        boolean enabled,
        String baseUrl,
        String clientId,
        String clientSecret,
        Duration timeout,
        int maxAttempts,
        Duration retryBackoff,
        /** Por quanto tempo o gestor resolvido de uma unidade e reaproveitado. */
        Duration recipientCacheTtl
) {
    public AdminCoreProperties {
        baseUrl = baseUrl == null ? "" : baseUrl.replaceAll("/+$", "");
        clientId = clientId == null ? "ms-inventory" : clientId;
        timeout = timeout == null ? Duration.ofSeconds(5) : timeout;
        maxAttempts = maxAttempts <= 0 ? 3 : maxAttempts;
        retryBackoff = retryBackoff == null ? Duration.ofMillis(500) : retryBackoff;
        recipientCacheTtl = recipientCacheTtl == null ? Duration.ofMinutes(10) : recipientCacheTtl;
    }

    public boolean isConfigured() {
        return enabled && !baseUrl.isBlank() && clientSecret != null && !clientSecret.isBlank();
    }
}
