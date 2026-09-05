package com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper;

import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.Category;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.CategoryNode;

@Component
public class CategoryMapper {

    public Category toDomain(CategoryNode node) {
        if (node == null) {
            return null;
        }
        return new Category(node.getId(), node.getUnitId(), node.getName(), node.getDescription(),
                node.getCreatedAt(), node.getUpdatedAt());
    }

    public CategoryNode toNode(Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryNode(category.getId(), category.getUnitId(), category.getName(), category.getDescription(),
                category.getCreatedAt(), category.getUpdatedAt());
    }
}
