package com.zera.ms_inventory.core.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Disposal;
import com.zera.ms_inventory.core.domain.valueobject.DisposedWeight;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;

/** Toda leitura e escrita e escopada por unidade. */
public interface DisposalRepository {
    Disposal save(Disposal disposal);
    Optional<Disposal> findById(UUID unitId, UUID id);
    PageResult<Disposal> findPage(UUID unitId, Pagination pagination);

    /** Pesos congelados dos itens descartados no periodo, com os materiais do modelo. */
    List<DisposedWeight> findDisposedWeights(UUID unitId, LocalDate from, LocalDate to);
}
