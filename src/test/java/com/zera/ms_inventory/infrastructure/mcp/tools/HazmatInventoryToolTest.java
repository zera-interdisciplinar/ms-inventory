package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Material;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.valueobject.MaterialCode;
import com.zera.ms_inventory.core.usecase.model.FindAllModels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HazmatInventoryToolTest {

    @Mock
    private FindAllModels findAllModels;

    private static final Material BATTERY = new Material(UUID.randomUUID(), MaterialCode.BATTERY,
            "Pilhas e baterias", true, true, "guia");
    private static final Material PLASTIC = new Material(UUID.randomUUID(), MaterialCode.PLASTIC,
            "Plástico", true, false, "guia");

    private Model model(String name, Material... materials) {
        return new Model(UUID.randomUUID(), Fixtures.UNIT, name, "Acme", 24, 60, Set.of(materials),
                null, null, Fixtures.category(Fixtures.UNIT));
    }

    @Test
    void shouldListOnlyModelsWithHazardousMaterials() {
        when(findAllModels.execute(Fixtures.UNIT)).thenReturn(List.of(
                model("Laptop", BATTERY, PLASTIC),
                model("Cadeira", PLASTIC)));

        List<HazmatInventoryTool.HazmatModel> result =
                new HazmatInventoryTool(findAllModels).getHazmatInventory(Fixtures.UNIT, null, null);

        assertEquals(1, result.size());
        assertEquals("Laptop", result.get(0).modelName);
        assertEquals("Electronics", result.get(0).categoryName);
        assertEquals(Set.of("Pilhas e baterias"), result.get(0).hazardousMaterials);
    }

    @Test
    void shouldApplyLimitAndOffset() {
        when(findAllModels.execute(Fixtures.UNIT)).thenReturn(List.of(
                model("A", BATTERY),
                model("B", BATTERY),
                model("C", BATTERY)));

        List<HazmatInventoryTool.HazmatModel> result =
                new HazmatInventoryTool(findAllModels).getHazmatInventory(Fixtures.UNIT, 1, 1);

        assertEquals(1, result.size());
        assertEquals("B", result.get(0).modelName);
    }

    @Test
    void shouldRejectMissingUnitIdInsteadOfFallingBackToAGlobalRead() {
        HazmatInventoryTool tool = new HazmatInventoryTool(findAllModels);

        assertThrows(IllegalArgumentException.class, () -> tool.getHazmatInventory(null, null, null));
        verifyNoInteractions(findAllModels);
    }
}
