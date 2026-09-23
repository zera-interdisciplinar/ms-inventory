package com.zera.ms_inventory.core.domain.entity;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.zera.ms_inventory.Fixtures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class UnitInventorySettingsTest {

    @Test
    void shouldStartNotConfigured() {
        UnitInventorySettings settings = UnitInventorySettings.notConfigured(Fixtures.UNIT);

        assertThat(settings.isConfigured()).isFalse();
        assertThat(settings.getStockCapacity()).isNull();
        assertThat(settings.getUpdatedAt()).isNull();
        assertThat(settings.occupancyPercent(10)).isEmpty();
    }

    @Test
    void shouldRecordWhoChangedTheCapacity() {
        UnitInventorySettings settings = UnitInventorySettings.notConfigured(Fixtures.UNIT);

        settings.changeCapacity(500, Fixtures.MANAGER);

        assertThat(settings.isConfigured()).isTrue();
        assertThat(settings.getStockCapacity()).isEqualTo(500);
        assertThat(settings.getUpdatedBy()).isEqualTo(Fixtures.MANAGER.userId());
        assertThat(settings.getUpdatedByName()).isEqualTo("Kevin Gestor");
        assertThat(settings.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldComputeTheOccupancy() {
        UnitInventorySettings settings = UnitInventorySettings.notConfigured(Fixtures.UNIT);
        settings.changeCapacity(200, Fixtures.MANAGER);

        assertThat(settings.occupancyPercent(50)).hasValueCloseTo(25.0, within(0.001));
        assertThat(settings.occupancyPercent(0)).hasValue(0.0);
    }

    /** Passar de 100% e informativo: significa estoque acima do que a unidade comporta. */
    @Test
    void shouldAllowOccupancyAboveOneHundred() {
        UnitInventorySettings settings = UnitInventorySettings.notConfigured(Fixtures.UNIT);
        settings.changeCapacity(10, Fixtures.MANAGER);

        assertThat(settings.occupancyPercent(15)).hasValueCloseTo(150.0, within(0.001));
    }

    /** Capacidade zero nao vira divisao por zero. */
    @Test
    void shouldNotComputeOccupancyWithZeroCapacity() {
        UnitInventorySettings settings = UnitInventorySettings.notConfigured(Fixtures.UNIT);
        settings.changeCapacity(0, Fixtures.MANAGER);

        assertThat(settings.isConfigured()).isTrue();
        assertThat(settings.occupancyPercent(5)).isEmpty();
    }

    @Test
    void shouldRejectUnitlessSettingsAndNegativeCapacity() {
        assertThatThrownBy(() -> UnitInventorySettings.notConfigured(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new UnitInventorySettings(UUID.randomUUID(), -1, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);

        UnitInventorySettings settings = UnitInventorySettings.notConfigured(Fixtures.UNIT);
        assertThatThrownBy(() -> settings.changeCapacity(-5, Fixtures.MANAGER))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(settings.isConfigured()).isFalse();
    }

    @Test
    void shouldAcceptClearingTheCapacity() {
        UnitInventorySettings settings = UnitInventorySettings.notConfigured(Fixtures.UNIT);
        settings.changeCapacity(100, Fixtures.MANAGER);

        settings.changeCapacity(null, Fixtures.MANAGER);

        assertThat(settings.isConfigured()).isFalse();
    }
}
