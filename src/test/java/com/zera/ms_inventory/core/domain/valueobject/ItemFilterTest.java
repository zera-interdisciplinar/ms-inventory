package com.zera.ms_inventory.core.domain.valueobject;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ItemFilterTest {

    @Test
    void shouldIgnoreABlankSearch() {
        assertThat(new ItemFilter(null, null, null, "   ").query()).isNull();
        assertThat(new ItemFilter(null, null, null, "  placa  ").query()).isEqualTo("placa");
    }

    @Test
    void shouldFilterNothingByDefault() {
        ItemFilter filtro = ItemFilter.none();

        assertThat(filtro.status()).isNull();
        assertThat(filtro.createdBy()).isNull();
        assertThat(filtro.condition()).isNull();
        assertThat(filtro.onlyEligibleForDisposal()).isFalse();
    }

    /** Recorte da Central de Trabalho: o que e do usuario logado. */
    @Test
    void shouldNarrowToTheAuthorPendencies() {
        UUID autor = UUID.randomUUID();
        ItemFilter filtro = ItemFilter.ownedBy(autor, ItemStatus.DRAFT);

        assertThat(filtro.createdBy()).isEqualTo(autor);
        assertThat(filtro.status()).isEqualTo(ItemStatus.DRAFT);
        assertThat(filtro.onlyEligibleForDisposal()).isFalse();
    }

    /** "Sem destino" e exatamente o que ainda pode ser descartado. */
    @Test
    void shouldReuseDisposalEligibilityForDamagedWithoutDestination() {
        ItemFilter filtro = ItemFilter.damagedWithoutDestination();

        assertThat(filtro.condition()).isEqualTo(ItemCondition.DAMAGED);
        assertThat(filtro.onlyEligibleForDisposal()).isTrue();
        assertThat(filtro.createdBy()).isNull();
        assertThat(filtro.status()).isNull();
    }

    @Test
    void shouldKeepTheShorterConstructorsWorking() {
        assertThat(new ItemFilter(ItemStatus.IN_STOCK, null, null, null).eligibleForDisposal()).isNull();
        assertThat(new ItemFilter(null, null, null, null, true).onlyEligibleForDisposal()).isTrue();
    }
}
