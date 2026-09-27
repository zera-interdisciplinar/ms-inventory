package com.zera.ms_inventory.infrastructure.prediction;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * O cabecalho {@code apikey} so aparece de verdade no fio, entao aqui o cliente fala com um
 * servidor HTTP de mentira em vez de um mock: quando a chamada passa pelo Kong, esse cabecalho e a
 * diferenca entre a previsao atualizar e o job levar 401 todo dia sem ninguem olhar.
 */
class PredictionRestClientTest {

    private HttpServer server;
    private final AtomicReference<String> receivedApiKey = new AtomicReference<>();

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/predict-batch", exchange -> {
            receivedApiKey.set(exchange.getRequestHeaders().getFirst("apikey"));
            byte[] body = "{\"results\":[]}".getBytes();
            exchange.getResponseHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private PredictionProperties properties(String apiKey) {
        return new PredictionProperties(true, "http://127.0.0.1:" + server.getAddress().getPort(),
                "/predict-batch", apiKey, "TROPICAL", Duration.ofSeconds(5), 100, 1,
                Duration.ofMillis(1));
    }

    private void callPredictBatch(PredictionProperties properties) {
        PredictionRestClient.of(properties).post()
                .uri("/predict-batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("devices", List.of()))
                .retrieve()
                .toBodilessEntity();
    }

    @Test
    void shouldSendTheApiKeyWhenTheCallGoesThroughTheGateway() {
        callPredictBatch(properties("zera1405"));

        assertThat(receivedApiKey.get()).isEqualTo("zera1405");
    }

    /** Direto no servico do cluster nao ha Kong; mandar um cabecalho vazio so confundiria o log. */
    @Test
    void shouldOmitTheApiKeyWhenItIsNotConfigured() {
        callPredictBatch(properties(""));

        assertThat(receivedApiKey.get()).isNull();
    }
}
