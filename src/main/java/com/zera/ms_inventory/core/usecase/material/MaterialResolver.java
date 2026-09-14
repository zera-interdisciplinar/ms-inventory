package com.zera.ms_inventory.core.usecase.material;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.Material;
import com.zera.ms_inventory.core.domain.exception.MaterialNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.MaterialCode;
import com.zera.ms_inventory.core.repository.MaterialRepository;

/** Converte codigos informados pelo app em materiais do catalogo, falhando no primeiro que nao existir. */
@Component
public class MaterialResolver {
    private final MaterialRepository materialRepository;

    public MaterialResolver(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    public Set<Material> resolve(Set<MaterialCode> codes) {
        List<Material> found = materialRepository.findAllByCodes(codes);
        Set<MaterialCode> missing = new HashSet<>(codes);
        found.forEach(material -> missing.remove(material.getCode()));
        if (!missing.isEmpty()) {
            throw new MaterialNotFoundException(missing.iterator().next());
        }
        return Set.copyOf(found);
    }
}
