package com.zera.ms_inventory.infrastructure.prediction;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.json.JsonMapper;
import com.zera.ms_inventory.core.domain.valueobject.PredictionInput;
import com.zera.ms_inventory.core.domain.valueobject.PredictionOutcome;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** Conversa com o preditivo contra um servidor MCP simulado, sem subir o contexto. */
class FailurePredictorGatewayTest {

    private static final String URL = "http://predictor/mcp";

    private PredictionProperties properties;
    private RestClient.Builder builder;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        properties = new PredictionProperties(true, "http://predictor", "/mcp", "TROPICAL",
                Duration.ofSeconds(1), 100, 2, Duration.ofMillis(1));
        builder = RestClient.builder().baseUrl("http://predictor");
        server = MockRestServiceServer.bindTo(builder).build();
    }

    private FailurePredictorGateway gateway() {
        return new FailurePredictorGateway(
                new McpToolCaller(builder.build(), JsonMapper.builder().build(), properties.endpoint()), properties);
    }

    private static PredictionInput input(String model) {
        return new PredictionInput(UUID.randomUUID(), "Notebooks", "Acme", model, "TROPICAL", 7, 2021,
                LocalDate.of(2022, 1, 15));
    }

    private static String structured(String results) {
        return """
                {"jsonrpc":"2.0","id":1,"result":{"structuredContent":{"result":%s},"isError":false}}
                """.formatted(results);
    }

    @Test
    void shouldCallTheBatchToolAndMapMonthsBackToEachItem() {
        PredictionInput first = input("Latitude 5420");
        PredictionInput second = input("ThinkPad T14");
        server.expect(requestTo(URL))
                .andExpect(method(POST))
                .andExpect(jsonPath("$.method").value("tools/call"))
                .andExpect(jsonPath("$.params.name").value("predict_time_to_failure_batch"))
                .andExpect(jsonPath("$.params.arguments.requests[0].model").value("Latitude 5420"))
                .andRespond(withSuccess(structured("[42.4, 7.6]"), MediaType.APPLICATION_JSON));

        List<PredictionOutcome> outcomes = gateway().predict(List.of(first, second));

        assertThat(outcomes).hasSize(2);
        assertThat(outcomes.get(0).itemId()).isEqualTo(first.itemId());
        assertThat(outcomes.get(0).monthsToFailure()).isEqualTo(42.4);
        assertThat(outcomes.get(1).itemId()).isEqualTo(second.itemId());
    }

    /** O preditivo descarta o itemId; mandar seria ruido, e a correspondencia e pela posicao. */
    @Test
    void shouldSendOnlyTheContractFields() {
        server.expect(requestTo(URL))
                .andExpect(jsonPath("$.params.arguments.requests[0].itemId").doesNotExist())
                .andExpect(jsonPath("$.params.arguments.requests[0].category").value("Notebooks"))
                .andExpect(jsonPath("$.params.arguments.requests[0].manufacturingDate").value(2021))
                .andExpect(jsonPath("$.params.arguments.requests[0].acquiredAt").value("2022-01-15"))
                .andExpect(jsonPath("$.params.arguments.requests[0].usageIntensity").value(7))
                .andRespond(withSuccess(structured("[12.0]"), MediaType.APPLICATION_JSON));

        gateway().predict(List.of(input("Latitude 5420")));

        server.verify();
    }

    /** O transporte streamable recusa o POST sem este Accept. */
    @Test
    void shouldAnnounceBothResponseFormats() {
        server.expect(requestTo(URL))
                .andExpect(header("Accept", "application/json, text/event-stream"))
                .andRespond(withSuccess(structured("[12.0]"), MediaType.APPLICATION_JSON));

        gateway().predict(List.of(input("Latitude 5420")));

        server.verify();
    }

    /** Um item invalido volta com erro na propria posicao e nao derruba os vizinhos. */
    @Test
    void shouldKeepTheGoodPredictionsWhenOneItemFails() {
        String results = "[10.0, {\"error\": \"manufacturingDate: field required\"}]";
        server.expect(requestTo(URL)).andRespond(withSuccess(structured(results), MediaType.APPLICATION_JSON));

        List<PredictionOutcome> outcomes = gateway().predict(List.of(input("A"), input("B")));

        assertThat(outcomes.get(0).isSuccess()).isTrue();
        assertThat(outcomes.get(1).isSuccess()).isFalse();
        assertThat(outcomes.get(1).error()).contains("manufacturingDate");
    }

    /** Servidor que nao preenche structuredContent manda o mesmo valor como texto em content. */
    @Test
    void shouldReadTheResultFromTheTextContentWhenThereIsNoStructuredContent() {
        String body = """
                {"jsonrpc":"2.0","id":1,"result":{"content":[{"type":"text","text":"[24.0]"}]}}
                """;
        server.expect(requestTo(URL)).andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        List<PredictionOutcome> outcomes = gateway().predict(List.of(input("A")));

        assertThat(outcomes.get(0).monthsToFailure()).isEqualTo(24.0);
    }

    @Test
    void shouldRetryBeforeGivingUpAndThenLeaveEveryItemUnpredicted() {
        server.expect(requestTo(URL)).andRespond(withServerError());
        server.expect(requestTo(URL)).andRespond(withServerError());

        List<PredictionOutcome> outcomes = gateway().predict(List.of(input("A")));

        server.verify();
        assertThat(outcomes).hasSize(1);
        assertThat(outcomes.get(0).isSuccess()).isFalse();
    }

    @Test
    void shouldRecoverOnTheSecondAttempt() {
        server.expect(requestTo(URL)).andRespond(withServerError());
        server.expect(requestTo(URL)).andRespond(withSuccess(structured("[5.0]"), MediaType.APPLICATION_JSON));

        assertThat(gateway().predict(List.of(input("A"))).get(0).isSuccess()).isTrue();
    }

    /** Erro declarado pelo proprio protocolo tambem e falha, ainda que o HTTP tenha sido 200. */
    @Test
    void shouldTreatAJsonRpcErrorAsAFailure() {
        String body = """
                {"jsonrpc":"2.0","id":1,"error":{"code":-32601,"message":"Unknown tool"}}
                """;
        server.expect(requestTo(URL)).andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
        server.expect(requestTo(URL)).andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        assertThat(gateway().predict(List.of(input("A"))).get(0).isSuccess()).isFalse();
    }

    /** Resposta mais curta que o lote deixa os itens sem par marcados, e nao deslocados. */
    @Test
    void shouldNotShiftItemsWhenTheResponseIsShorterThanTheBatch() {
        server.expect(requestTo(URL)).andRespond(withSuccess(structured("[10.0]"), MediaType.APPLICATION_JSON));

        List<PredictionOutcome> outcomes = gateway().predict(List.of(input("A"), input("B")));

        assertThat(outcomes.get(0).isSuccess()).isTrue();
        assertThat(outcomes.get(1).isSuccess()).isFalse();
    }

    @Test
    void shouldNotCallThePredictorForAnEmptyBatch() {
        assertThat(gateway().predict(List.of())).isEmpty();
        server.verify();
    }
}
