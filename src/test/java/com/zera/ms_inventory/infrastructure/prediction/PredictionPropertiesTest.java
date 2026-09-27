package com.zera.ms_inventory.infrastructure.prediction;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PredictionPropertiesTest {

    private static PredictionProperties bare(boolean enabled, String baseUrl) {
        return new PredictionProperties(enabled, baseUrl, null, null, null, null, 0, 0, null);
    }

    @Test
    void shouldFillTheDefaultsOfTheContract() {
        PredictionProperties properties = bare(true, "http://predictor");

        assertThat(properties.endpoint()).isEqualTo("/predict-batch");
        assertThat(properties.climateZone()).isEqualTo("TROPICAL");
        assertThat(properties.batchSize()).isEqualTo(100);
        assertThat(properties.maxAttempts()).isEqualTo(3);
        assertThat(properties.timeout()).isEqualTo(Duration.ofSeconds(30));
        assertThat(properties.apiKey()).isEmpty();
    }

    /** Barra sobrando na base concatenaria com a do endpoint e produziria {@code //predict-batch}. */
    @Test
    void shouldTrimTrailingSlashesFromTheBaseUrl() {
        assertThat(bare(true, "http://predictor///").baseUrl()).isEqualTo("http://predictor");
    }

    /** Lote configurado acima do limite do endpoint viraria 422 em toda execucao do job. */
    @Test
    void shouldCapTheBatchSizeAtTheEndpointLimit() {
        PredictionProperties properties = new PredictionProperties(true, "http://predictor", null, null,
                null, null, 5000, 0, null);

        assertThat(properties.batchSize()).isEqualTo(PredictionProperties.MAX_BATCH_SIZE);
    }

    @Test
    void shouldNotConsiderItselfConfiguredWithoutAnAddressOrWhenDisabled() {
        assertThat(bare(true, "").isConfigured()).isFalse();
        assertThat(bare(false, "http://predictor").isConfigured()).isFalse();
        assertThat(bare(true, "http://predictor").isConfigured()).isTrue();
    }
}
