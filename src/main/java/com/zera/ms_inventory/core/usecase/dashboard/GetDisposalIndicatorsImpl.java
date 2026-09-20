package com.zera.ms_inventory.core.usecase.dashboard;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.valueobject.DisposalIndicators;
import com.zera.ms_inventory.core.domain.valueobject.DisposedWeight;
import com.zera.ms_inventory.core.repository.DisposalRepository;

@Service
public class GetDisposalIndicatorsImpl implements GetDisposalIndicators {

    private static final int MESES_PADRAO = 12;

    private final DisposalRepository disposalRepository;

    public GetDisposalIndicatorsImpl(DisposalRepository disposalRepository) {
        this.disposalRepository = disposalRepository;
    }

    /**
     * O periodo anterior tem a mesma duracao e termina na vespera do inicio: e o unico recorte que
     * torna a variacao comparavel.
     */
    @Override
    public DisposalIndicators execute(UUID unitId, LocalDate from, LocalDate to) {
        LocalDate fim = to != null ? to : LocalDate.now();
        LocalDate inicio = from != null ? from : fim.minusMonths(MESES_PADRAO - 1L).withDayOfMonth(1);
        if (inicio.isAfter(fim)) {
            throw new IllegalArgumentException("from cannot be after to");
        }

        long dias = ChronoUnit.DAYS.between(inicio, fim);
        LocalDate fimAnterior = inicio.minusDays(1);
        LocalDate inicioAnterior = fimAnterior.minusDays(dias);

        List<DisposedWeight> periodo = disposalRepository.findDisposedWeights(unitId, inicio, fim);
        List<DisposedWeight> anterior = disposalRepository.findDisposedWeights(unitId, inicioAnterior, fimAnterior);
        return DisposalIndicators.of(inicio, fim, periodo, anterior);
    }
}
