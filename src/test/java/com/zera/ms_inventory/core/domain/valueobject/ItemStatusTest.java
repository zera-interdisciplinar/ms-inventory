package com.zera.ms_inventory.core.domain.valueobject;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class ItemStatusTest {

    @ParameterizedTest
    @CsvSource({
            "DRAFT, PENDING_APPROVAL", "DRAFT, IN_STOCK", "DRAFT, REMOVED",
            "PENDING_APPROVAL, IN_STOCK", "PENDING_APPROVAL, REJECTED",
            "REJECTED, PENDING_APPROVAL", "REJECTED, IN_STOCK",
            "IN_STOCK, IN_MAINTENANCE", "IN_STOCK, DISPOSED", "IN_STOCK, REMOVED",
            "IN_MAINTENANCE, AWAITING_EVALUATION",
            "AWAITING_EVALUATION, IN_STOCK", "AWAITING_EVALUATION, DISPOSED",
            "REMOVED, IN_STOCK", "REMOVED, DRAFT"})
    void shouldAllowTheTransitionsOfTheFlow(ItemStatus from, ItemStatus to) {
        assertThat(from.canTransitionTo(to)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
            "DRAFT, DISPOSED", "DRAFT, IN_MAINTENANCE", "DRAFT, REJECTED",
            "PENDING_APPROVAL, IN_MAINTENANCE", "PENDING_APPROVAL, DISPOSED",
            "IN_STOCK, PENDING_APPROVAL", "IN_STOCK, AWAITING_EVALUATION", "IN_STOCK, DRAFT",
            "IN_MAINTENANCE, IN_STOCK", "IN_MAINTENANCE, DISPOSED",
            "AWAITING_EVALUATION, IN_MAINTENANCE",
            "REMOVED, DISPOSED"})
    void shouldRejectTransitionsOutsideTheFlow(ItemStatus from, ItemStatus to) {
        assertThat(from.canTransitionTo(to)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(ItemStatus.class)
    void shouldRejectNullAndStayingInTheSameStatus(ItemStatus status) {
        assertThat(status.canTransitionTo(null)).isFalse();
        assertThat(status.canTransitionTo(status)).isFalse();
    }

    /** O descarte encerra a vida do item: nem remocao logica sai de la (ZERA-247). */
    @Test
    void shouldLeaveDisposedAsATerminalStatus() {
        assertThat(ItemStatus.DISPOSED.allowedTransitions()).isEmpty();
    }

    @Test
    void shouldReachRemovedFromEveryStatusButDisposed() {
        assertThat(Arrays.stream(ItemStatus.values())
                .filter(s -> s != ItemStatus.DISPOSED && s != ItemStatus.REMOVED)
                .filter(s -> !s.canTransitionTo(ItemStatus.REMOVED))
                .toList()).isEmpty();
    }

    /** REMOVED volta para qualquer estado ativo, porque a restauracao devolve o item ao anterior. */
    @Test
    void shouldRestoreFromRemovedToEveryActiveStatus() {
        assertThat(ItemStatus.REMOVED.allowedTransitions())
                .allMatch(ItemStatus::isActive)
                .hasSize(6);
    }

    @Test
    void shouldTreatOnlyDisposedAndRemovedAsInactive() {
        assertThat(Arrays.stream(ItemStatus.values()).filter(s -> !s.isActive()).toList())
                .containsExactlyInAnyOrder(ItemStatus.DISPOSED, ItemStatus.REMOVED);
    }

    /** Toda migracao futura de status precisa declarar as transicoes do estado novo. */
    @Test
    void shouldDeclareTransitionsForEveryStatus() {
        for (ItemStatus status : ItemStatus.values()) {
            assertThat(status.allowedTransitions()).as("transicoes de %s", status).isNotNull();
        }
    }
}
