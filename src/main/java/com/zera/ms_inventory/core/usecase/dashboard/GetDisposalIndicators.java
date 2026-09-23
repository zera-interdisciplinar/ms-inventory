package com.zera.ms_inventory.core.usecase.dashboard;

import java.time.LocalDate;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.valueobject.DisposalIndicators;

public interface GetDisposalIndicators {
    /** Datas nulas assumem os ultimos 12 meses, que e o recorte do painel. */
    DisposalIndicators execute(UUID unitId, LocalDate from, LocalDate to);
}
