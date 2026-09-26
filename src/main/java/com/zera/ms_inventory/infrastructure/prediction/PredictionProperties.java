package com.zera.ms_inventory.infrastructure.prediction;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Integracao com o {@code ml-failure-predictor}. Desligada por padrao: sem o servico no ar, a
 * previsao simplesmente nao atualiza, e nada mais no inventario depende dela para funcionar.
 *
 * <p>{@code climateZone} e configuracao do servico e nao campo do item porque hoje todas as
 * unidades operam no mesmo clima; virar dado por unidade e uma mudanca de produto, nao de
 * integracao.</p>
 */
@ConfigurationProperties(prefix = "zera.prediction")
public record PredictionProperties(
        boolean enabled,
        String baseUrl,
        /** Caminho do endpoint MCP do preditivo; o servidor dele publica em {@code /mcp}. */
        String endpoint,
        String climateZone,
        Duration timeout,
        int batchSize,
        int maxAttempts,
        Duration retryBackoff
) {
    public PredictionProperties {
        baseUrl = baseUrl == null ? "" : baseUrl.replaceAll("/+$", "");
        endpoint = endpoint == null || endpoint.isBlank() ? "/mcp" : endpoint;
        climateZone = climateZone == null || climateZone.isBlank() ? "TROPICAL" : climateZone;
        // o lote e um forward pass so; o timeout precisa caber o lote inteiro, nao um item
        timeout = timeout == null ? Duration.ofSeconds(30) : timeout;
        batchSize = batchSize <= 0 ? 100 : batchSize;
        maxAttempts = maxAttempts <= 0 ? 3 : maxAttempts;
        retryBackoff = retryBackoff == null ? Duration.ofMillis(500) : retryBackoff;
    }

    public boolean isConfigured() {
        return enabled && !baseUrl.isBlank();
    }
}
