package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.usecase.model.FindAllModels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HazmatInventoryToolTest {

    @Mock
    private FindAllModels findAllModels;

    @Test
    void shouldFilterModelsWithHazardousMaterials() {
        Model hazmat = new Model(UUID.randomUUID(), "Battery", "Acme", 12, 60, Set.of("Lithium"));
        Model clean = new Model(UUID.randomUUID(), "Hammer", "Acme", 12, 60, Set.of());
        when(findAllModels.execute()).thenReturn(List.of(hazmat, clean));

        HazmatInventoryTool tool = new HazmatInventoryTool(findAllModels);

        List<HazmatInventoryTool.HazmatModel> result = tool.getHazmatInventory(null, null);

        assertEquals(1, result.size());
        assertEquals(hazmat.getId(), result.get(0).modelId);
    }

    @Test
    void shouldApplyLimitAndOffset() {
        Model m1 = new Model(UUID.randomUUID(), "A", "Acme", 12, 60, Set.of("X"));
        Model m2 = new Model(UUID.randomUUID(), "B", "Acme", 12, 60, Set.of("Y"));
        when(findAllModels.execute()).thenReturn(List.of(m1, m2));

        HazmatInventoryTool tool = new HazmatInventoryTool(findAllModels);

        List<HazmatInventoryTool.HazmatModel> result = tool.getHazmatInventory(1, 1);

        assertEquals(1, result.size());
        assertEquals(m2.getId(), result.get(0).modelId);
    }
}
