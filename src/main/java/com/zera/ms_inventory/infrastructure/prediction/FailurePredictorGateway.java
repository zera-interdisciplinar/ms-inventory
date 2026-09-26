package com.zera.ms_inventory.infrastructure.prediction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tools.jackson.databind.JsonNode;
import com.zera.ms_inventory.core.domain.valueobject.PredictionInput;
import com.zera.ms_inventory.core.domain.valueobject.PredictionOutcome;
import com.zera.ms_inventory.core.repository.FailurePredictionGateway;

/**
 * Fala com o {@code ml-failure-predictor} pela ferramenta MCP {@code predict_time_to_failure_batch}.
 *
 * <p>O modelo responde por posicao, um resultado por entrada, e um item invalido volta como
 * {@code {"error": ...}} no lugar do numero, sem derrubar os vizinhos. E por isso que o
 * {@code itemId} nao vai no payload: o preditivo o descartaria, e a correspondencia e pelo
 * indice.</p>
 */
class FailurePredictorGateway implements FailurePredictionGateway {

    private static final String TOOL = "predict_time_to_failure_batch";

    private static final Logger log = LoggerFactory.getLogger(FailurePredictorGateway.class);

    private final McpToolCaller caller;
    private final int maxAttempts;
    private final long backoffMillis;

    FailurePredictorGateway(McpToolCaller caller, PredictionProperties properties) {
        this.caller = caller;
        this.maxAttempts = properties.maxAttempts();
        this.backoffMillis = properties.retryBackoff().toMillis();
    }

    @Override
    public List<PredictionOutcome> predict(List<PredictionInput> inputs) {
        if (inputs.isEmpty()) {
            return List.of();
        }
        JsonNode results = callWithRetry(inputs);
        if (results == null || !results.isArray()) {
            return failAll(inputs, "predictor returned an unexpected payload");
        }

        List<PredictionOutcome> outcomes = new ArrayList<>(inputs.size());
        for (int i = 0; i < inputs.size(); i++) {
            outcomes.add(outcomeOf(inputs.get(i), i < results.size() ? results.get(i) : null));
        }
        return outcomes;
    }

    private static PredictionOutcome outcomeOf(PredictionInput input, JsonNode result) {
        if (result == null || result.isNull()) {
            return PredictionOutcome.failed(input.itemId(), "no result for this item");
        }
        if (result.isNumber()) {
            return PredictionOutcome.predicted(input.itemId(), result.asDouble());
        }
        return PredictionOutcome.failed(input.itemId(), result.path("error").asText(result.toString()));
    }

    /**
     * Rede instavel nao pode custar um dia de previsao, mas insistir sem intervalo so empilha
     * requisicao em cima de um servico que ja esta em dificuldade; por isso a espera cresce.
     */
    private JsonNode callWithRetry(List<PredictionInput> inputs) {
        Map<String, Object> arguments = Map.of("requests", inputs.stream()
                .map(FailurePredictorGateway::payloadOf)
                .toList());

        RuntimeException last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return caller.call(TOOL, arguments);
            } catch (RuntimeException e) {
                last = e;
                log.warn("Prediction attempt {}/{} failed: {}", attempt, maxAttempts, e.getMessage());
                if (attempt < maxAttempts) {
                    sleep(backoffMillis * attempt);
                }
            }
        }
        log.warn("Predictor unreachable; predictions kept as they are", last);
        return null;
    }

    /** Ordem e nomes sao os do contrato do preditivo; o {@code itemId} fica de fora de proposito. */
    private static Map<String, Object> payloadOf(PredictionInput input) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("category", input.category());
        payload.put("manufacturer", input.manufacturer());
        payload.put("model", input.model());
        payload.put("climateZone", input.climateZone());
        payload.put("usageIntensity", input.usageIntensity());
        payload.put("manufacturingDate", input.manufacturingYear());
        payload.put("acquiredAt", input.acquiredAt().toString());
        return payload;
    }

    private static List<PredictionOutcome> failAll(List<PredictionInput> inputs, String reason) {
        return inputs.stream().map(input -> PredictionOutcome.failed(input.itemId(), reason)).toList();
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
