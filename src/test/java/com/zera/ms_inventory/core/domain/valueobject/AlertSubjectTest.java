package com.zera.ms_inventory.core.domain.valueobject;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.zera.ms_inventory.Fixtures;

import static org.assertj.core.api.Assertions.assertThat;

class AlertSubjectTest {

    @Test
    void shouldDescribeAnItemByItsShortCode() {
        UUID itemId = UUID.randomUUID();
        AlertSubject subject = new AlertSubject(itemId, "100001", "Notebook");

        assertThat(subject.isUnitWide()).isFalse();
        assertThat(subject.describe()).isEqualTo("o item 100001 (Notebook)");
        assertThat(subject.dedupKey(Fixtures.UNIT)).isEqualTo(itemId);
    }

    @Test
    void shouldFallBackToTheIdWithoutAShortCode() {
        UUID itemId = UUID.randomUUID();

        assertThat(new AlertSubject(itemId, null, null).describe()).isEqualTo("o item " + itemId);
        assertThat(new AlertSubject(itemId, "100001", "  ").describe()).isEqualTo("o item 100001");
    }

    /** A regra de ocupacao nao tem item: o assunto e a unidade, e e por ela que deduplica. */
    @Test
    void shouldUseTheUnitAsSubjectWhenThereIsNoItem() {
        AlertSubject subject = AlertSubject.unit();

        assertThat(subject.isUnitWide()).isTrue();
        assertThat(subject.describe()).isEqualTo("a unidade");
        assertThat(subject.dedupKey(Fixtures.UNIT)).isEqualTo(Fixtures.UNIT);
    }
}
