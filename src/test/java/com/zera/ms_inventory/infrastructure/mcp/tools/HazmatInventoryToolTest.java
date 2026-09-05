package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.usecase.model.FindAllModels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HazmatInventoryToolTest {

    @Mock
    private FindAllModels findAllModels;

    private Model model(String name, Set<String> hazmat) {
        return new Model(UUID.randomUUID(), Fixtures.UNIT, name, "Acme", 24, 60, hazmat,
                Fixtures.category(Fixtures.UNIT));
    }

    @Test
    void shouldListOnlyModelsWithHazardousMaterials() {
        when(findAllModels.execute(Fixtures.UNIT)).thenReturn(List.of(
                model("Laptop", Set.of("Lithium")),
                model("Cadeira", Set.of())));

        List<HazmatInventoryTool.HazmatModel> result =
                new HazmatInventoryTool(findAllModels).getHazmatInventory(Fixtures.UNIT, null, null);

        assertEquals(1, result.size());
        assertEquals("Laptop", result.get(0).modelName);
        assertEquals("Electronics", result.get(0).categoryName);
    }

    @Test
    void shouldApplyLimitAndOffset() {
        when(findAllModels.execute(Fixtures.UNIT)).thenReturn(List.of(
                model("A", Set.of("Lithium")),
                model("B", Set.of("Mercury")),
                model("C", Set.of("Cobalt"))));

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
