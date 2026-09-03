package com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper;

import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.ModelNode;

@Component
public class ModelMapper {

    private final CategoryMapper categoryMapper;

    public ModelMapper(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public Model toDomain(ModelNode node) {
        if (node == null) {
            return null;
        }
        return new Model(node.getId(), node.getUnitId(), node.getName(), node.getManufacturer(),
                node.getWarrantyMonths(), node.getExpectedLifespanMonths(), node.getHazardousMaterials(),
                categoryMapper.toDomain(node.getCategory()), node.getCreatedAt(), node.getUpdatedAt());
    }

    public ModelNode toNode(Model model) {
        if (model == null) {
            return null;
        }
        return new ModelNode(model.getId(), model.getUnitId(), model.getName(), model.getManufacturer(),
                model.getWarrantyMonths(), model.getExpectedLifespanMonths(), model.getHazardousMaterials(),
                model.getCreatedAt(), model.getUpdatedAt());
    }
}
