package com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.ModelNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ModelMapperTest {

    private final ModelMapper mapper = new ModelMapper(new CategoryMapper());

    @Test
    void shouldMapDomainToNodeAndBack() {
        UUID id = UUID.randomUUID();
        Model model = Fixtures.model(id, Fixtures.UNIT);

        ModelNode node = mapper.toNode(model);
        Model result = mapper.toDomain(node);

        assertEquals(model.getId(), result.getId());
        assertEquals(Fixtures.UNIT, result.getUnitId());
        assertEquals(model.getName(), result.getName());
        assertEquals(model.getManufacturer(), result.getManufacturer());
        assertEquals(model.getWarrantyMonths(), result.getWarrantyMonths());
        assertEquals(model.getExpectedLifespanMonths(), result.getExpectedLifespanMonths());
        assertEquals(model.getHazardousMaterials(), result.getHazardousMaterials());
        assertEquals(model.getCreatedAt(), result.getCreatedAt());
        assertEquals(model.getUpdatedAt(), result.getUpdatedAt());
    }

    @Test
    void shouldLeaveCategoryToThePersistenceLayer() {
        // toNode nao carrega a relacao de proposito: o repo anexa o CategoryNode ja persistido,
        // senao o cascade do SDN zeraria o embedding da Category.
        ModelNode node = mapper.toNode(Fixtures.model(Fixtures.UNIT));

        assertNull(node.getCategory());
    }

    @Test
    void shouldMapCategoryBackWhenTheNodeHasOne() {
        ModelNode node = mapper.toNode(Fixtures.model(Fixtures.UNIT));
        node.setCategory(new CategoryMapper().toNode(Fixtures.category(Fixtures.UNIT)));

        assertNotNull(mapper.toDomain(node).getCategory());
    }

    @Test
    void shouldMapNullsToNull() {
        assertNull(mapper.toNode(null));
        assertNull(mapper.toDomain(null));
    }
}
