package com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.zera.ms_inventory.core.domain.entity.Category;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.CategoryNode;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CategoryMapperTest {

    private final CategoryMapper mapper = new CategoryMapper();

    @Test
    void shouldMapDomainToNodeAndBack() {
        UUID id = UUID.randomUUID();
        Category category = new Category(id, "Electronics", "Devices");

        CategoryNode node = mapper.toNode(category);
        Category result = mapper.toDomain(node);

        assertEquals(category.getId(), result.getId());
        assertEquals(category.getName(), result.getName());
        assertEquals(category.getDescription(), result.getDescription());
        assertEquals(category.getCreatedAt(), result.getCreatedAt());
        assertEquals(category.getUpdatedAt(), result.getUpdatedAt());
    }
}
