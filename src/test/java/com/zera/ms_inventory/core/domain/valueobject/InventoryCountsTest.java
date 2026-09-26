package com.zera.ms_inventory.core.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class InventoryCountsTest {

    @Test
    void shouldComputeTheStockChange() {
        assertThat(new InventoryCounts(120, 100, 0, 0, 0, 0, 0).activeItemsChangePercent())
                .hasValueCloseTo(20.0, within(0.001));
        assertThat(new InventoryCounts(80, 100, 0, 0, 0, 0, 0).activeItemsChangePercent())
                .hasValueCloseTo(-20.0, within(0.001));
    }

    /** Unidade que comecou vazia nao tem base de comparacao. */
    @Test
    void shouldLeaveTheChangeEmptyWithoutPreviousStock() {
        assertThat(new InventoryCounts(10, 0, 0, 0, 0, 0, 0).activeItemsChangePercent()).isEmpty();
    }

    @Test
    void shouldStartZeroed() {
        InventoryCounts vazio = InventoryCounts.empty();

        assertThat(vazio.activeItems()).isZero();
        assertThat(vazio.pendingApproval()).isZero();
        assertThat(vazio.inMaintenance()).isZero();
        assertThat(vazio.awaitingEvaluation()).isZero();
        assertThat(vazio.draft()).isZero();
        assertThat(vazio.rejected()).isZero();
        assertThat(vazio.activeItemsChangePercent()).isEmpty();
    }
}
