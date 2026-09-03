package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.usecase.item.SemanticSearchInventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SemanticSearchInventoryToolTest {

    @Mock
    private SemanticSearchInventory semanticSearchInventory;

    @Test
    void shouldSearchWithTheDefaultLimit() {
        when(semanticSearchInventory.execute(Fixtures.UNIT, "bateria de litio", 10))
                .thenReturn(List.of(Fixtures.item(Fixtures.UNIT)));

        List<Item> result = new SemanticSearchInventoryTool(semanticSearchInventory)
                .semanticSearch(Fixtures.UNIT, "bateria de litio", null);

        assertEquals(1, result.size());
        verify(semanticSearchInventory).execute(Fixtures.UNIT, "bateria de litio", 10);
    }

    @Test
    void shouldHonourAnExplicitLimit() {
        when(semanticSearchInventory.execute(Fixtures.UNIT, "cadeira", 3)).thenReturn(List.of());

        new SemanticSearchInventoryTool(semanticSearchInventory).semanticSearch(Fixtures.UNIT, "cadeira", 3);

        verify(semanticSearchInventory).execute(Fixtures.UNIT, "cadeira", 3);
    }

    @Test
    void shouldFallBackToTheDefaultLimitWhenItIsNotPositive() {
        when(semanticSearchInventory.execute(Fixtures.UNIT, "cadeira", 10)).thenReturn(List.of());

        new SemanticSearchInventoryTool(semanticSearchInventory).semanticSearch(Fixtures.UNIT, "cadeira", 0);

        verify(semanticSearchInventory).execute(Fixtures.UNIT, "cadeira", 10);
    }

    @Test
    void shouldRejectMissingUnitIdInsteadOfSearchingEveryUnit() {
        SemanticSearchInventoryTool tool = new SemanticSearchInventoryTool(semanticSearchInventory);

        assertThrows(IllegalArgumentException.class, () -> tool.semanticSearch(null, "qualquer", 5));
        verifyNoInteractions(semanticSearchInventory);
    }
}
