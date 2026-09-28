package com.zera.ms_inventory.core.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.valueobject.AlertSeverity;
import com.zera.ms_inventory.core.domain.valueobject.AlertSubject;
import com.zera.ms_inventory.core.domain.valueobject.RuleKind;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RuleAlertTest {

    private final UUID ruleId = UUID.randomUUID();

    @Test
    void shouldApplyDefaultsForMomentAndSeverity() {
        RuleAlert alerta = new RuleAlert(Fixtures.UNIT, ruleId, RuleKind.STALE_ITEM,
                AlertSubject.unit(), null, "parado", null);

        assertThat(alerta.occurredAt()).isNotNull();
        assertThat(alerta.severity()).isEqualTo(AlertSeverity.MEDIUM);
    }

    @Test
    void shouldKeepTheInformedMoment() {
        LocalDateTime quando = LocalDateTime.of(2026, 9, 26, 3, 0);

        assertThat(new RuleAlert(Fixtures.UNIT, ruleId, RuleKind.STALE_ITEM, AlertSubject.unit(),
                AlertSeverity.LOW, "x", quando).occurredAt()).isEqualTo(quando);
    }

    @Test
    void shouldRequireUnitRuleKindAndSubject() {
        assertThatThrownBy(() -> new RuleAlert(null, ruleId, RuleKind.STALE_ITEM, AlertSubject.unit(),
                null, "x", null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RuleAlert(Fixtures.UNIT, null, RuleKind.STALE_ITEM,
                AlertSubject.unit(), null, "x", null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RuleAlert(Fixtures.UNIT, ruleId, null, AlertSubject.unit(),
                null, "x", null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RuleAlert(Fixtures.UNIT, ruleId, RuleKind.STALE_ITEM, null,
                null, "x", null)).isInstanceOf(IllegalArgumentException.class);
    }
}
