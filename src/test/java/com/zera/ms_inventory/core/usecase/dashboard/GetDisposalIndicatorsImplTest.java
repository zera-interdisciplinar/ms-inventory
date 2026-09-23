package com.zera.ms_inventory.core.usecase.dashboard;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.valueobject.DestinationType;
import com.zera.ms_inventory.core.domain.valueobject.DisposalIndicators;
import com.zera.ms_inventory.core.domain.valueobject.DisposedWeight;
import com.zera.ms_inventory.core.domain.valueobject.MaterialCode;
import com.zera.ms_inventory.core.repository.DisposalRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetDisposalIndicatorsImplTest {

    @Mock private DisposalRepository disposalRepository;

    private GetDisposalIndicatorsImpl useCase() {
        return new GetDisposalIndicatorsImpl(disposalRepository);
    }

    /** O periodo anterior tem a mesma duracao e termina na vespera do inicio. */
    @Test
    void shouldReadTheEquivalentPreviousPeriod() {
        LocalDate from = LocalDate.parse("2026-03-01");
        LocalDate to = LocalDate.parse("2026-03-31");
        when(disposalRepository.findDisposedWeights(any(), any(), any())).thenReturn(List.of());

        useCase().execute(Fixtures.UNIT, from, to);

        verify(disposalRepository).findDisposedWeights(Fixtures.UNIT, from, to);
        // 30 dias de intervalo: o anterior vai de 29/01 a 28/02
        verify(disposalRepository).findDisposedWeights(Fixtures.UNIT,
                LocalDate.parse("2026-01-29"), LocalDate.parse("2026-02-28"));
    }

    @Test
    void shouldAssumeTheLastTwelveMonthsWithoutDates() {
        when(disposalRepository.findDisposedWeights(any(), any(), any())).thenReturn(List.of());

        DisposalIndicators indicadores = useCase().execute(Fixtures.UNIT, null, null);

        assertThat(indicadores.to()).isEqualTo(LocalDate.now());
        assertThat(indicadores.from()).isEqualTo(LocalDate.now().minusMonths(11).withDayOfMonth(1));
        assertThat(indicadores.monthlyWeightKg()).hasSize(12);
    }

    @Test
    void shouldComputeFromWhatTheRepositoryReturns() {
        LocalDate from = LocalDate.parse("2026-01-01");
        LocalDate to = LocalDate.parse("2026-01-31");
        when(disposalRepository.findDisposedWeights(Fixtures.UNIT, from, to)).thenReturn(List.of(
                new DisposedWeight(DestinationType.RECYCLING, LocalDate.parse("2026-01-05"), 3.0,
                        List.of(MaterialCode.METAL)),
                new DisposedWeight(DestinationType.LANDFILL, LocalDate.parse("2026-01-06"), 1.0,
                        List.of(MaterialCode.GLASS))));
        when(disposalRepository.findDisposedWeights(Fixtures.UNIT, LocalDate.parse("2025-12-01"),
                LocalDate.parse("2025-12-31"))).thenReturn(List.of());

        DisposalIndicators indicadores = useCase().execute(Fixtures.UNIT, from, to);

        assertThat(indicadores.totalWeightKg()).isEqualTo(4.0);
        assertThat(indicadores.recyclingRatePercent()).isEqualTo(75.0);
    }

    @Test
    void shouldRejectAnInvertedRange() {
        GetDisposalIndicatorsImpl useCase = useCase();

        assertThatThrownBy(() -> useCase.execute(Fixtures.UNIT, LocalDate.parse("2026-05-01"),
                LocalDate.parse("2026-01-01"))).isInstanceOf(IllegalArgumentException.class);
    }
}
