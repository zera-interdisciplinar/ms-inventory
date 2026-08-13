package com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper;

import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.Category;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.CategoryNode;

@Component
public class CategoryMapper {

    public Category toDomain(CategoryNode node) {
        return new Category(node.getId(), node.getName(), node.getDescription(), node.getCreatedAt(), node.getUpdatedAt());
    }

    public CategoryNode toNode(Category category) {
        return new CategoryNode(category.getId(), category.getName(), category.getDescription(),
                category.getCreatedAt(), category.getUpdatedAt());
    }
}
