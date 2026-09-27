package com.zera.ms_inventory.infrastructure.prediction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;
import com.zera.ms_inventory.core.domain.valueobject.PredictionInput;
import com.zera.ms_inventory.core.domain.valueobject.PredictionOutcome;
import com.zera.ms_inventory.core.repository.FailurePredictionGateway;

/**
 * Fala com o preditivo pelo endpoint REST de lote ({@code POST /predict-batch}).
 *
 * <p>O mesmo servico tambem expoe uma ferramenta MCP equivalente, e a primeira versao desta
 * integracao usava ela. O time de IA pediu a troca com um motivo que procede: a ferramenta MCP
 * existe para um agente LLM decidir em tempo de execucao se e quando chamar, e o envelope do
 * protocolo (JSON-RPC, {@code structuredContent}, versao negociada) e parte desse contrato de
 * agente, nao de um RPC estavel. Nosso caller e um job agendado que faz sempre a mesma chamada,
 * sem LLM no meio; montar aquele envelope a mao aqui nos deixaria expostos a qualquer mudanca de
 * versao do protocolo MCP, que ninguem trataria como quebra de contrato.</p>
 *
 * <p>A resposta vem na ordem dos itens enviados, e um item invalido volta com
 * {@code estimated_remaining_months: null} e {@code error} preenchido apenas naquela posicao, sem
 * derrubar o lote. E por isso que o {@code itemId} nao vai no payload: o preditivo o descartaria,
 * e a correspondencia e pelo indice.</p>
 */
class FailurePredictorGateway implements FailurePredictionGateway {

    /**
     * Limite do endpoint: acima disso ele responde 422. O corte mora aqui, e nao no caso de uso,
     * porque e uma restricao do transporte — assim nenhuma configuracao de lote consegue furar.
     */
    private static final int MAX_DEVICES_PER_CALL = 500;

    private static final Logger log = LoggerFactory.getLogger(FailurePredictorGateway.class);

    private final RestClient restClient;
    private final String endpoint;
    private final int maxAttempts;
    private final long backoffMillis;

    FailurePredictorGateway(RestClient restClient, PredictionProperties properties) {
        this.restClient = restClient;
        this.endpoint = properties.endpoint();
        this.maxAttempts = properties.maxAttempts();
        this.backoffMillis = properties.retryBackoff().toMillis();
    }

    @Override
    public List<PredictionOutcome> predict(List<PredictionInput> inputs) {
        if (inputs.isEmpty()) {
            return List.of();
        }
        List<PredictionOutcome> outcomes = new ArrayList<>(inputs.size());
        for (int start = 0; start < inputs.size(); start += MAX_DEVICES_PER_CALL) {
            List<PredictionInput> slice =
                    inputs.subList(start, Math.min(start + MAX_DEVICES_PER_CALL, inputs.size()));
            outcomes.addAll(predictOneCall(slice));
        }
        return outcomes;
    }

    private List<PredictionOutcome> predictOneCall(List<PredictionInput> inputs) {
        JsonNode response = callWithRetry(inputs);
        if (response == null) {
            return failAll(inputs, "predictor unreachable");
        }
        JsonNode results = response.path("results");
        if (!results.isArray()) {
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
        JsonNode months = result.path("estimated_remaining_months");
        if (months.isNumber()) {
            return PredictionOutcome.predicted(input.itemId(), months.asDouble());
        }
        JsonNode error = result.path("error");
        return PredictionOutcome.failed(input.itemId(),
                error.isTextual() ? error.asString() : "prediction came back empty");
    }

    /**
     * Rede instavel nao pode custar um dia de previsao, mas insistir sem intervalo so empilha
     * requisicao em cima de um servico que ja esta em dificuldade; por isso a espera cresce.
     */
    private JsonNode callWithRetry(List<PredictionInput> inputs) {
        Map<String, Object> body = Map.of("devices", inputs.stream()
                .map(FailurePredictorGateway::payloadOf)
                .toList());

        RuntimeException last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return restClient.post()
                        .uri(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(JsonNode.class);
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

    /** Nomes sao os do contrato do preditivo; o {@code itemId} fica de fora de proposito. */
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
