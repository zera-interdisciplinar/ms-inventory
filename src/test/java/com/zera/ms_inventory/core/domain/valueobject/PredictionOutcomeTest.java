package com.zera.ms_inventory.core.domain.valueobject;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PredictionOutcomeTest {

    private static final UUID ITEM = UUID.randomUUID();
    private static final LocalDate ACQUIRED = LocalDate.of(2022, 3, 15);

    @Test
    void shouldCountMonthsFromTheAcquisitionDate() {
        assertThat(PredictionOutcome.predicted(ITEM, 18).failureDateFrom(ACQUIRED))
                .isEqualTo(LocalDate.of(2023, 9, 15));
    }

    @Test
    void shouldRoundToTheNearestMonth() {
        assertThat(PredictionOutcome.predicted(ITEM, 11.6).failureDateFrom(ACQUIRED))
                .isEqualTo(ACQUIRED.plusMonths(12));
        assertThat(PredictionOutcome.predicted(ITEM, 11.4).failureDateFrom(ACQUIRED))
                .isEqualTo(ACQUIRED.plusMonths(11));
    }

    /** Previsao vencida significa "ja passou da hora", nao uma data antes da compra do item. */
    @Test
    void shouldNotProduceADateBeforeTheItemExisted() {
        assertThat(PredictionOutcome.predicted(ITEM, -5).failureDateFrom(ACQUIRED)).isEqualTo(ACQUIRED);
        assertThat(PredictionOutcome.predicted(ITEM, 0).failureDateFrom(ACQUIRED)).isEqualTo(ACQUIRED);
    }

    @Test
    void shouldNotProduceADateWithoutABasisOrWithoutAPrediction() {
        assertThat(PredictionOutcome.predicted(ITEM, 12).failureDateFrom(null)).isNull();
        assertThat(PredictionOutcome.failed(ITEM, "nope").failureDateFrom(ACQUIRED)).isNull();
    }

    @Test
    void shouldAlwaysCarryAReasonWhenItFails() {
        assertThat(PredictionOutcome.failed(ITEM, null).error()).isEqualTo("unknown error");
        assertThat(PredictionOutcome.failed(ITEM, null).isSuccess()).isFalse();
        assertThat(PredictionOutcome.predicted(ITEM, 1).isSuccess()).isTrue();
    }
}
