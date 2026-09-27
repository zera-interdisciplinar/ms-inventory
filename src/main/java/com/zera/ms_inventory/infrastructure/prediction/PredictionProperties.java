package com.zera.ms_inventory.infrastructure.prediction;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Integracao com o preditivo de quebra. Desligada por padrao: sem o servico no ar, a previsao
 * simplesmente nao atualiza, e nada mais no inventario depende dela para funcionar.
 *
 * <p>{@code climateZone} e configuracao do servico e nao campo do item porque hoje todas as
 * unidades operam no mesmo clima; virar dado por unidade e uma mudanca de produto, nao de
 * integracao.</p>
 */
@ConfigurationProperties(prefix = "zera.prediction")
public record PredictionProperties(
        boolean enabled,
        String baseUrl,
        /** Caminho do endpoint REST de lote do preditivo. */
        String endpoint,
        /** Chave do Kong, quando a chamada passa pelo gateway; em branco nao envia o cabecalho. */
        String apiKey,
        String climateZone,
        Duration timeout,
        int batchSize,
        int maxAttempts,
        Duration retryBackoff
) {
    /** O endpoint recusa lotes acima disso com 422. */
    static final int MAX_BATCH_SIZE = 500;

    public PredictionProperties {
        baseUrl = baseUrl == null ? "" : baseUrl.replaceAll("/+$", "");
        endpoint = endpoint == null || endpoint.isBlank() ? "/predict-batch" : endpoint;
        apiKey = apiKey == null ? "" : apiKey.trim();
        climateZone = climateZone == null || climateZone.isBlank() ? "TROPICAL" : climateZone;
        // o lote e um forward pass so; o timeout precisa caber o lote inteiro, nao um item
        timeout = timeout == null ? Duration.ofSeconds(30) : timeout;
        batchSize = batchSize <= 0 ? 100 : Math.min(batchSize, MAX_BATCH_SIZE);
        maxAttempts = maxAttempts <= 0 ? 3 : maxAttempts;
        retryBackoff = retryBackoff == null ? Duration.ofMillis(500) : retryBackoff;
    }

    public boolean isConfigured() {
        return enabled && !baseUrl.isBlank();
    }
}
