package com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.ModelNode;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModelMapperTest {

    private final ModelMapper mapper = new ModelMapper();

    @Test
    void shouldMapDomainToNodeAndBack() {
        UUID id = UUID.randomUUID();
        Model model = new Model(id, "Laptop X1", "Acme", 24, 60, Set.of("Lithium"));

        ModelNode node = mapper.toNode(model);
        Model result = mapper.toDomain(node);

        assertEquals(model.getId(), result.getId());
        assertEquals(model.getName(), result.getName());
        assertEquals(model.getManufacturer(), result.getManufacturer());
        assertEquals(model.getWarrantyMonths(), result.getWarrantyMonths());
        assertEquals(model.getExpectedLifespanMonths(), result.getExpectedLifespanMonths());
        assertEquals(model.getHazardousMaterials(), result.getHazardousMaterials());
        assertEquals(model.getCreatedAt(), result.getCreatedAt());
        assertEquals(model.getUpdatedAt(), result.getUpdatedAt());
    }
}
