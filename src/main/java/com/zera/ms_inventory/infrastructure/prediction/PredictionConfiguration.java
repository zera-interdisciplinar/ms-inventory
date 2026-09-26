package com.zera.ms_inventory.infrastructure.prediction;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.databind.json.JsonMapper;

/**
 * O cliente so existe quando a integracao esta ligada e configurada. Criar o bean de qualquer jeito
 * deixaria um cliente apontado para lugar nenhum esperando a primeira chamada falhar.
 */
@Configuration
@ConditionalOnProperty(prefix = "zera.prediction", name = "enabled", havingValue = "true")
public class PredictionConfiguration {

    @Bean
    FailurePredictorGateway failurePredictorGateway(PredictionProperties properties, JsonMapper jsonMapper) {
        if (!properties.isConfigured()) {
            throw new IllegalStateException(
                    "zera.prediction.enabled=true requires zera.prediction.base-url");
        }
        McpToolCaller caller = new McpToolCaller(PredictionRestClient.of(properties), jsonMapper,
                properties.endpoint());
        return new FailurePredictorGateway(caller, properties);
    }
}
