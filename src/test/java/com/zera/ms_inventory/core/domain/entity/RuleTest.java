package com.zera.ms_inventory.core.domain.entity;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.valueobject.RuleKind;
import com.zera.ms_inventory.core.domain.valueobject.RuleLimitUnit;
import com.zera.ms_inventory.core.domain.valueobject.RuleTarget;
import com.zera.ms_inventory.core.domain.valueobject.RuleTargetType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RuleTest {

    private Rule wholeUnitRule() {
        return new Rule(UUID.randomUUID(), Fixtures.UNIT, "Garantia", RuleKind.WARRANTY_EXPIRATION, 30,
                RuleLimitUnit.DAYS, null, true);
    }

    @Test
    void shouldStartAppliedToTheWholeUnit() {
        Rule rule = wholeUnitRule();

        assertThat(rule.appliesToWholeUnit()).isTrue();
        assertThat(rule.getUnitId()).isEqualTo(Fixtures.UNIT);
        assertThat(rule.getCreatedAt()).isNotNull();
        assertThat(rule.getUpdatedAt()).isEqualTo(rule.getCreatedAt());
    }

    @Test
    void shouldNarrowToAModelAndBackToTheUnit() {
        Rule rule = wholeUnitRule();
        UUID modelId = UUID.randomUUID();

        rule.changeTarget(RuleTarget.model(modelId));
        assertThat(rule.getTarget().type()).isEqualTo(RuleTargetType.MODEL);
        assertThat(rule.getTarget().id()).isEqualTo(modelId);
        assertThat(rule.appliesToWholeUnit()).isFalse();

        rule.changeTarget(null);
        assertThat(rule.appliesToWholeUnit()).isTrue();
    }

    @Test
    void shouldRenameActivateAndDeactivate() {
        Rule rule = wholeUnitRule();

        rule.rename("Garantia vencendo");
        rule.deactivate();
        assertThat(rule.getName()).isEqualTo("Garantia vencendo");
        assertThat(rule.isActive()).isFalse();

        rule.activate();
        assertThat(rule.isActive()).isTrue();
    }

    @Test
    void shouldChangeTheLimit() {
        Rule rule = wholeUnitRule();

        rule.changeLimit(60, RuleLimitUnit.DAYS);

        assertThat(rule.getLimitValue()).isEqualTo(60);
        assertThat(rule.getLimitUnit()).isEqualTo(RuleLimitUnit.DAYS);
    }

    /** PERCENT so faz sentido em limite relativo; garantia em 20% nao quer dizer nada. */
    @Test
    void shouldAcceptPercentOnlyForRelativeLimits() {
        Rule estoque = new Rule(UUID.randomUUID(), Fixtures.UNIT, "Estoque cheio",
                RuleKind.STOCK_QUANTITY_LIMIT, 90, RuleLimitUnit.PERCENT, null, true);
        assertThat(estoque.getLimitUnit()).isEqualTo(RuleLimitUnit.PERCENT);

        assertThatThrownBy(() -> new Rule(UUID.randomUUID(), Fixtures.UNIT, "Garantia",
                RuleKind.WARRANTY_EXPIRATION, 20, RuleLimitUnit.PERCENT, null, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PERCENT");

        Rule garantia = wholeUnitRule();
        assertThatThrownBy(() -> garantia.changeLimit(20, RuleLimitUnit.PERCENT))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /** Numero sem unidade nao da para interpretar, e unidade sem numero nao limita nada. */
    @Test
    void shouldRefuseAHalfLimit() {
        assertThatThrownBy(() -> new Rule(UUID.randomUUID(), Fixtures.UNIT, "x", RuleKind.STALE_ITEM, 90,
                null, null, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("both limitValue and limitUnit");
        assertThatThrownBy(() -> new Rule(UUID.randomUUID(), Fixtures.UNIT, "x", RuleKind.STALE_ITEM, null,
                RuleLimitUnit.DAYS, null, true))
                .isInstanceOf(IllegalArgumentException.class);

        Rule regra = wholeUnitRule();
        assertThatThrownBy(() -> regra.changeLimit(90, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> regra.changeLimit(null, RuleLimitUnit.DAYS))
                .isInstanceOf(IllegalArgumentException.class);
        // o limite antigo continua de pe apos a recusa
        assertThat(regra.getLimitValue()).isEqualTo(30);
    }

    @Test
    void shouldAllowClearingTheWholeLimit() {
        Rule regra = wholeUnitRule();

        regra.changeLimit(null, null);

        assertThat(regra.getLimitValue()).isNull();
        assertThat(regra.getLimitUnit()).isNull();
    }

    @Test
    void shouldRequireUnitAndKindAndRefuseNegativeLimit() {
        assertThatThrownBy(() -> new Rule(UUID.randomUUID(), null, "x", RuleKind.STALE_ITEM, 1,
                RuleLimitUnit.DAYS, null, true)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Rule(UUID.randomUUID(), Fixtures.UNIT, "x", null, 1,
                RuleLimitUnit.DAYS, null, true)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Rule(UUID.randomUUID(), Fixtures.UNIT, "x", RuleKind.STALE_ITEM, -1,
                RuleLimitUnit.DAYS, null, true)).isInstanceOf(IllegalArgumentException.class);
    }

    /** Ha regra que so liga e desliga, sem numero: o reciclavel indo para o aterro e uma delas. */
    @Test
    void shouldAcceptARuleWithoutLimit() {
        Rule rule = new Rule(UUID.randomUUID(), Fixtures.UNIT, "Reciclavel no aterro",
                RuleKind.RECYCLABLE_TO_LANDFILL, null, null, null, true);

        assertThat(rule.getLimitValue()).isNull();
        assertThat(rule.getLimitUnit()).isNull();
    }

    @Test
    void shouldRequireBothPartsOfATarget() {
        assertThatThrownBy(() -> new RuleTarget(RuleTargetType.MODEL, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RuleTarget(null, UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(RuleTarget.of(null, null)).isNull();
        assertThat(RuleTarget.category(Fixtures.UNIT).type()).isEqualTo(RuleTargetType.CATEGORY);
    }
}
