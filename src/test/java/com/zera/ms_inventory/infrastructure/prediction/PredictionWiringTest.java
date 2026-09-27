package com.zera.ms_inventory.infrastructure.prediction;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import com.zera.ms_inventory.core.domain.valueobject.PredictionInput;
import com.zera.ms_inventory.core.repository.FailurePredictionGateway;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A integracao com o admin-core ja custou um contexto que nao subia por criar cliente HTTP com a
 * integracao desligada; aqui isso e verificado em vez de descoberto na subida.
 */
class PredictionWiringTest {

    @Configuration
    @EnableConfigurationProperties(PredictionProperties.class)
    static class Properties {
    }

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(Properties.class, PredictionConfiguration.class,
                    LoggingFailurePredictionGateway.class);

    @Test
    void shouldNotCreateAnyHttpClientWhenTheIntegrationIsOff() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(FailurePredictionGateway.class);
            assertThat(context).hasSingleBean(LoggingFailurePredictionGateway.class);
            assertThat(context).doesNotHaveBean(FailurePredictorGateway.class);
        });
    }

    @Test
    void shouldUseThePredictorWhenTheIntegrationIsConfigured() {
        runner.withPropertyValues("zera.prediction.enabled=true",
                        "zera.prediction.base-url=http://predictor")
                .run(context -> {
                    assertThat(context).hasSingleBean(FailurePredictorGateway.class);
                    assertThat(context).doesNotHaveBean(LoggingFailurePredictionGateway.class);
                });
    }

    /** Ligar sem endereco e erro de configuracao, e falhar na subida e melhor que falhar de noite. */
    @Test
    void shouldRefuseToStartWhenEnabledWithoutAnAddress() {
        runner.withPropertyValues("zera.prediction.enabled=true")
                .run(context -> assertThat(context).hasFailed());
    }

    /** Desligada, a previsao nao e inventada: o item fica sem previsao, e o painel diz isso. */
    @Test
    void shouldNotMakeUpPredictionsWhenDisabled() {
        var outcomes = new LoggingFailurePredictionGateway().predict(List.of(
                new PredictionInput(UUID.randomUUID(), "Notebooks", "Acme", "Latitude", "TROPICAL", 5,
                        2021, LocalDate.now())));

        assertThat(outcomes).hasSize(1);
        assertThat(outcomes.get(0).isSuccess()).isFalse();
        assertThat(outcomes.get(0).error()).contains("disabled");
    }
}
