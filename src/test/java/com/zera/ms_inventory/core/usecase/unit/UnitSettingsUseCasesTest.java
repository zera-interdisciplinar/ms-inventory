package com.zera.ms_inventory.core.usecase.unit;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.UnitInventorySettings;
import com.zera.ms_inventory.core.repository.UnitInventorySettingsRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnitSettingsUseCasesTest {

    @Mock private UnitInventorySettingsRepository repository;

    private UnitSettingsUseCases useCase() {
        return new UnitSettingsUseCases(repository);
    }

    /** Unidade que nunca configurou nao e erro: responde a configuracao vazia. */
    @Test
    void shouldReturnEmptySettingsForAUnitThatNeverConfigured() {
        when(repository.findByUnit(Fixtures.UNIT)).thenReturn(Optional.empty());

        UnitInventorySettings settings = useCase().execute(Fixtures.UNIT);

        assertThat(settings.getUnitId()).isEqualTo(Fixtures.UNIT);
        assertThat(settings.isConfigured()).isFalse();
    }

    @Test
    void shouldReturnTheStoredSettings() {
        UnitInventorySettings stored = new UnitInventorySettings(Fixtures.UNIT, 300, Fixtures.MANAGER.userId(),
                "Kevin Gestor", null);
        when(repository.findByUnit(Fixtures.UNIT)).thenReturn(Optional.of(stored));

        assertThat(useCase().execute(Fixtures.UNIT).getStockCapacity()).isEqualTo(300);
    }

    @Test
    void shouldCreateTheSettingsOnTheFirstCapacity() {
        when(repository.findByUnit(Fixtures.UNIT)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        UnitInventorySettings settings = useCase().execute(Fixtures.UNIT, 800, Fixtures.MANAGER);

        assertThat(settings.getStockCapacity()).isEqualTo(800);
        ArgumentCaptor<UnitInventorySettings> salvo = ArgumentCaptor.forClass(UnitInventorySettings.class);
        verify(repository).save(salvo.capture());
        assertThat(salvo.getValue().getUpdatedByName()).isEqualTo("Kevin Gestor");
    }

    @Test
    void shouldUpdateTheCapacityKeepingTheUnit() {
        UnitInventorySettings stored = new UnitInventorySettings(Fixtures.UNIT, 100, null, null, null);
        when(repository.findByUnit(Fixtures.UNIT)).thenReturn(Optional.of(stored));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        UnitInventorySettings settings = useCase().execute(Fixtures.UNIT, 250, Fixtures.MANAGER);

        assertThat(settings.getUnitId()).isEqualTo(Fixtures.UNIT);
        assertThat(settings.getStockCapacity()).isEqualTo(250);
    }
}
