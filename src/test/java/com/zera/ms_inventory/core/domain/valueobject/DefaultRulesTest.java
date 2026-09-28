package com.zera.ms_inventory.core.domain.valueobject;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Rule;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultRulesTest {

    /** Toda unidade nova precisa de alerta funcionando sem ninguem configurar nada. */
    @Test
    void shouldCoverEveryRuleKind() {
        List<Rule> padrao = DefaultRules.forUnit(Fixtures.UNIT);

        assertThat(padrao).extracting(Rule::getKind)
                .containsExactlyInAnyOrderElementsOf(Arrays.asList(RuleKind.values()));
    }

    @Test
    void shouldCreateThemActiveForTheWholeUnit() {
        List<Rule> padrao = DefaultRules.forUnit(Fixtures.UNIT);

        assertThat(padrao).allSatisfy(rule -> {
            assertThat(rule.getUnitId()).isEqualTo(Fixtures.UNIT);
            assertThat(rule.isActive()).isTrue();
            assertThat(rule.appliesToWholeUnit()).isTrue();
            assertThat(rule.getName()).isNotBlank();
            assertThat(rule.getId()).isNotNull();
        });
    }

    /** O limite do estoque e o unico relativo, entao e o unico em PERCENT. */
    @Test
    void shouldUsePercentOnlyForTheStockLimit() {
        List<Rule> padrao = DefaultRules.forUnit(Fixtures.UNIT);

        assertThat(padrao).filteredOn(rule -> rule.getLimitUnit() == RuleLimitUnit.PERCENT)
                .singleElement()
                .satisfies(rule -> {
                    assertThat(rule.getKind()).isEqualTo(RuleKind.STOCK_QUANTITY_LIMIT);
                    assertThat(rule.getLimitValue()).isEqualTo(90);
                });
    }

    /**
     * Duas semeaduras simultaneas gravam os mesmos oito nos, nao dezesseis: o save e um MERGE por
     * id, entao id deterministico resolve a corrida sem trava.
     */
    @Test
    void shouldGenerateTheSameIdsForTheSameUnit() {
        List<Rule> primeira = DefaultRules.forUnit(Fixtures.UNIT);
        List<Rule> segunda = DefaultRules.forUnit(Fixtures.UNIT);

        assertThat(segunda).extracting(Rule::getId)
                .containsExactlyElementsOf(primeira.stream().map(Rule::getId).toList());
        assertThat(primeira).extracting(Rule::getId).doesNotHaveDuplicates();
    }

    @Test
    void shouldDeriveTheIdFromUnitAndKind() {
        assertThat(DefaultRules.idOf(Fixtures.UNIT, RuleKind.STALE_ITEM))
                .isEqualTo(DefaultRules.idOf(Fixtures.UNIT, RuleKind.STALE_ITEM))
                .isNotEqualTo(DefaultRules.idOf(Fixtures.UNIT, RuleKind.WARRANTY_EXPIRATION))
                .isNotEqualTo(DefaultRules.idOf(Fixtures.OTHER_UNIT, RuleKind.STALE_ITEM));
    }

    @Test
    void shouldGiveEachUnitItsOwnRules() {
        List<Rule> primeira = DefaultRules.forUnit(Fixtures.UNIT);
        List<Rule> segunda = DefaultRules.forUnit(Fixtures.OTHER_UNIT);

        assertThat(segunda).allMatch(rule -> rule.getUnitId().equals(Fixtures.OTHER_UNIT));
        assertThat(primeira).extracting(Rule::getId)
                .doesNotContainAnyElementsOf(segunda.stream().map(Rule::getId).toList());
    }

    @Test
    void shouldAcceptPercentOnlyOnRelativeKinds() {
        assertThat(RuleKind.STOCK_QUANTITY_LIMIT.acceptsPercent()).isTrue();
        assertThat(Arrays.stream(RuleKind.values()).filter(RuleKind::acceptsPercent).toList())
                .containsExactly(RuleKind.STOCK_QUANTITY_LIMIT);
    }
}
