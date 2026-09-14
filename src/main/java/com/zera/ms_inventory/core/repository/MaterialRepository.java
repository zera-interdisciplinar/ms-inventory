package com.zera.ms_inventory.core.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.zera.ms_inventory.core.domain.entity.Material;
import com.zera.ms_inventory.core.domain.valueobject.MaterialCode;

/** Catalogo global: somente leitura pela API, sem escopo de unidade. */
public interface MaterialRepository {
    List<Material> findAll();
    Optional<Material> findByCode(MaterialCode code);
    List<Material> findAllByCodes(Set<MaterialCode> codes);
}
