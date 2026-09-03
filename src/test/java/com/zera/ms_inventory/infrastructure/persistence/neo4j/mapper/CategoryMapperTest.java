package com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Category;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.CategoryNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CategoryMapperTest {

    private final CategoryMapper mapper = new CategoryMapper();

    @Test
    void shouldMapDomainToNodeAndBack() {
        UUID id = UUID.randomUUID();
        Category category = Fixtures.category(id, Fixtures.UNIT);

        CategoryNode node = mapper.toNode(category);
        Category result = mapper.toDomain(node);

        assertEquals(category.getId(), result.getId());
        assertEquals(Fixtures.UNIT, result.getUnitId());
        assertEquals(category.getName(), result.getName());
        assertEquals(category.getDescription(), result.getDescription());
        assertEquals(category.getCreatedAt(), result.getCreatedAt());
        assertEquals(category.getUpdatedAt(), result.getUpdatedAt());
    }

    @Test
    void shouldMapNullsToNull() {
        assertNull(mapper.toNode(null));
        assertNull(mapper.toDomain(null));
    }
}
