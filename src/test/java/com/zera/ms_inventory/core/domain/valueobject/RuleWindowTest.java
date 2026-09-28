package com.zera.ms_inventory.core.domain.valueobject;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RuleWindowTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 9, 26);

    @Test
    void shouldLookForwardForExpirationRules() {
        assertThat(RuleWindow.forward(HOJE, 30, RuleLimitUnit.DAYS)).isEqualTo(LocalDate.of(2026, 10, 26));
        assertThat(RuleWindow.forward(HOJE, 2, RuleLimitUnit.MONTHS)).isEqualTo(LocalDate.of(2026, 11, 26));
    }

    @Test
    void shouldLookBackwardForPermanenceRules() {
        assertThat(RuleWindow.backward(HOJE, 30, RuleLimitUnit.DAYS)).isEqualTo(LocalDate.of(2026, 8, 27));
        assertThat(RuleWindow.backward(HOJE, 6, RuleLimitUnit.MONTHS)).isEqualTo(LocalDate.of(2026, 3, 26));
    }

    /** UNITS e PERCENT nao sao janelas de tempo: a regra que os usa nao compara datas. */
    @Test
    void shouldReturnNullForNonTemporalUnits() {
        assertThat(RuleWindow.forward(HOJE, 8, RuleLimitUnit.UNITS)).isNull();
        assertThat(RuleWindow.backward(HOJE, 90, RuleLimitUnit.PERCENT)).isNull();
    }

    @Test
    void shouldReturnNullWithoutAReferenceOrLimit() {
        assertThat(RuleWindow.forward(null, 30, RuleLimitUnit.DAYS)).isNull();
        assertThat(RuleWindow.forward(HOJE, null, RuleLimitUnit.DAYS)).isNull();
        assertThat(RuleWindow.forward(HOJE, 30, null)).isNull();
        assertThat(RuleWindow.backward(null, null, null)).isNull();
    }
}
