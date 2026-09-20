package com.zera.ms_inventory.core.repository;

import java.util.Optional;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Disposal;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;

/** Toda leitura e escrita e escopada por unidade. */
public interface DisposalRepository {
    Disposal save(Disposal disposal);
    Optional<Disposal> findById(UUID unitId, UUID id);
    PageResult<Disposal> findPage(UUID unitId, Pagination pagination);
}
