package com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.Material;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.ModelNode;

@Component
public class ModelMapper {

    private final CategoryMapper categoryMapper;
    private final MaterialMapper materialMapper = new MaterialMapper();

    public ModelMapper(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public Model toDomain(ModelNode node) {
        if (node == null) {
            return null;
        }
        Set<Material> materials = node.getMaterials() == null ? Set.of()
                : node.getMaterials().stream().map(materialMapper::toDomain).collect(Collectors.toSet());
        return new Model(node.getId(), node.getUnitId(), node.getName(), node.getManufacturer(),
                node.getWarrantyMonths(), node.getExpectedLifespanMonths(), node.getHazardousMaterials(), materials,
                node.getEstimatedWeightKg(), node.getNotes(), categoryMapper.toDomain(node.getCategory()),
                node.getCreatedAt(), node.getUpdatedAt());
    }

    /**
     * Categoria e materiais ficam de fora de proposito: o repositorio anexa os nos ja persistidos,
     * senao o cascade do SDN sobrescreveria a Category (embedding) e o catalogo de materiais.
     */
    public ModelNode toNode(Model model) {
        if (model == null) {
            return null;
        }
        ModelNode node = new ModelNode(model.getId(), model.getUnitId(), model.getName(), model.getManufacturer(),
                model.getWarrantyMonths(), model.getExpectedLifespanMonths(), model.getHazardousMaterials(),
                model.getCreatedAt(), model.getUpdatedAt());
        node.setEstimatedWeightKg(model.getEstimatedWeightKg());
        node.setNotes(model.getNotes());
        return node;
    }
}
