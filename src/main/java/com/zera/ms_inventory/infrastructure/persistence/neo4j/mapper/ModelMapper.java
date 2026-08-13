package com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper;

import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.ModelNode;

@Component
public class ModelMapper {

    public Model toDomain(ModelNode node) {
        return new Model(node.getId(), node.getName(), node.getManufacturer(), node.getWarrantyMonths(),
                node.getExpectedLifespanMonths(), node.getHazardousMaterials(), node.getCreatedAt(), node.getUpdatedAt());
    }

    public ModelNode toNode(Model model) {
        return new ModelNode(model.getId(), model.getName(), model.getManufacturer(), model.getWarrantyMonths(),
                model.getExpectedLifespanMonths(), model.getHazardousMaterials(), model.getCreatedAt(), model.getUpdatedAt());
    }
}
