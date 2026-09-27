package com.zera.ms_inventory.infrastructure.prediction;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.zera.ms_inventory.core.domain.valueobject.PredictionInput;
import com.zera.ms_inventory.core.domain.valueobject.PredictionOutcome;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** Conversa com o preditivo contra um servidor simulado, sem subir o contexto. */
class FailurePredictorGatewayTest {

    private static final String URL = "http://predictor/predict-batch";

    private PredictionProperties properties;
    private RestClient.Builder builder;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        properties = new PredictionProperties(true, "http://predictor", "/predict-batch", "",
                "TROPICAL", Duration.ofSeconds(1), 100, 2, Duration.ofMillis(1));
        builder = RestClient.builder().baseUrl("http://predictor");
        server = MockRestServiceServer.bindTo(builder).build();
    }

    private FailurePredictorGateway gateway() {
        return new FailurePredictorGateway(builder.build(), properties);
    }

    private static PredictionInput input(String model) {
        return new PredictionInput(UUID.randomUUID(), "notebook", "Dell", model, "TROPICAL", 8, 2022,
                LocalDate.of(2023, 1, 15));
    }

    private static String results(String items) {
        return "{\"results\": %s}".formatted(items);
    }

    private static String predicted(double months) {
        return "{\"estimated_remaining_months\": %s, \"error\": null}".formatted(months);
    }

    @Test
    void shouldPostTheBatchAndMapMonthsBackToEachItem() {
        PredictionInput first = input("Latitude 5420");
        PredictionInput second = input("ThinkPad T14");
        server.expect(requestTo(URL))
                .andExpect(method(POST))
                .andExpect(jsonPath("$.devices[0].model").value("Latitude 5420"))
                .andExpect(jsonPath("$.devices[1].model").value("ThinkPad T14"))
                .andRespond(withSuccess(results("[%s, %s]".formatted(predicted(42.4), predicted(7.6))),
                        MediaType.APPLICATION_JSON));

        List<PredictionOutcome> outcomes = gateway().predict(List.of(first, second));

        assertThat(outcomes).hasSize(2);
        assertThat(outcomes.get(0).itemId()).isEqualTo(first.itemId());
        assertThat(outcomes.get(0).monthsToFailure()).isEqualTo(42.4);
        assertThat(outcomes.get(1).itemId()).isEqualTo(second.itemId());
        assertThat(outcomes.get(1).monthsToFailure()).isEqualTo(7.6);
    }

    /** O preditivo descarta o itemId; mandar seria ruido, e a correspondencia e pela posicao. */
    @Test
    void shouldSendOnlyTheContractFieldsUnderDevices() {
        server.expect(requestTo(URL))
                .andExpect(jsonPath("$.devices[0].itemId").doesNotExist())
                .andExpect(jsonPath("$.devices[0].category").value("notebook"))
                .andExpect(jsonPath("$.devices[0].manufacturer").value("Dell"))
                .andExpect(jsonPath("$.devices[0].climateZone").value("TROPICAL"))
                .andExpect(jsonPath("$.devices[0].usageIntensity").value(8))
                .andExpect(jsonPath("$.devices[0].manufacturingDate").value(2022))
                .andExpect(jsonPath("$.devices[0].acquiredAt").value("2023-01-15"))
                .andRespond(withSuccess(results("[" + predicted(12.0) + "]"), MediaType.APPLICATION_JSON));

        gateway().predict(List.of(input("Latitude 5420")));

        server.verify();
    }

    /** Item invalido volta com erro na propria posicao e nao derruba os vizinhos. */
    @Test
    void shouldKeepTheGoodPredictionsWhenOneItemFails() {
        String failed = "{\"estimated_remaining_months\": null, "
                + "\"error\": \"manufacturingDate: field required\"}";
        server.expect(requestTo(URL))
                .andRespond(withSuccess(results("[%s, %s]".formatted(predicted(10.0), failed)),
                        MediaType.APPLICATION_JSON));

        List<PredictionOutcome> outcomes = gateway().predict(List.of(input("A"), input("B")));

        assertThat(outcomes.get(0).isSuccess()).isTrue();
        assertThat(outcomes.get(1).isSuccess()).isFalse();
        assertThat(outcomes.get(1).error()).contains("manufacturingDate");
    }

    /** Sem numero e sem erro, o item fica sem previsao em vez de virar uma data inventada. */
    @Test
    void shouldFailTheItemWhenBothMonthsAndErrorAreEmpty() {
        server.expect(requestTo(URL)).andRespond(withSuccess(
                results("[{\"estimated_remaining_months\": null, \"error\": null}]"),
                MediaType.APPLICATION_JSON));

        List<PredictionOutcome> outcomes = gateway().predict(List.of(input("A")));

        assertThat(outcomes.get(0).isSuccess()).isFalse();
        assertThat(outcomes.get(0).error()).isNotBlank();
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
        server.expect(requestTo(URL)).andRespond(withSuccess(results("[" + predicted(5.0) + "]"),
                MediaType.APPLICATION_JSON));

        assertThat(gateway().predict(List.of(input("A"))).get(0).isSuccess()).isTrue();
    }

    /** Resposta mais curta que o lote deixa os itens sem par marcados, e nao deslocados. */
    @Test
    void shouldNotShiftItemsWhenTheResponseIsShorterThanTheBatch() {
        server.expect(requestTo(URL)).andRespond(withSuccess(results("[" + predicted(10.0) + "]"),
                MediaType.APPLICATION_JSON));

        List<PredictionOutcome> outcomes = gateway().predict(List.of(input("A"), input("B")));

        assertThat(outcomes.get(0).isSuccess()).isTrue();
        assertThat(outcomes.get(1).isSuccess()).isFalse();
    }

    @Test
    void shouldFailEveryItemWhenThePayloadHasNoResults() {
        server.expect(requestTo(URL)).andRespond(withSuccess("{\"unexpected\": true}",
                MediaType.APPLICATION_JSON));

        assertThat(gateway().predict(List.of(input("A"))).get(0).isSuccess()).isFalse();
    }

    /**
     * O endpoint recusa lotes acima de 500 com 422, entao o gateway parte sozinho: quem chama nao
     * precisa conhecer o limite do transporte, e nenhuma configuracao de lote consegue fura-lo.
     */
    @Test
    void shouldSplitBatchesBiggerThanTheEndpointLimit() {
        List<PredictionInput> inputs = new ArrayList<>();
        for (int i = 0; i < 501; i++) {
            inputs.add(input("Latitude 5420"));
        }
        String fullPage = "[" + String.join(",", java.util.Collections.nCopies(500, predicted(12.0))) + "]";
        server.expect(times(1), requestTo(URL))
                .andExpect(jsonPath("$.devices[499]").exists())
                .andExpect(jsonPath("$.devices[500]").doesNotExist())
                .andRespond(withSuccess(results(fullPage), MediaType.APPLICATION_JSON));
        server.expect(times(1), requestTo(URL))
                .andExpect(jsonPath("$.devices[1]").doesNotExist())
                .andRespond(withSuccess(results("[" + predicted(9.0) + "]"), MediaType.APPLICATION_JSON));

        List<PredictionOutcome> outcomes = gateway().predict(inputs);

        server.verify();
        assertThat(outcomes).hasSize(501);
        assertThat(outcomes).allMatch(PredictionOutcome::isSuccess);
        assertThat(outcomes.get(500).monthsToFailure()).isEqualTo(9.0);
    }

    @Test
    void shouldNotCallThePredictorForAnEmptyBatch() {
        assertThat(gateway().predict(List.of())).isEmpty();
        server.verify();
    }
}
