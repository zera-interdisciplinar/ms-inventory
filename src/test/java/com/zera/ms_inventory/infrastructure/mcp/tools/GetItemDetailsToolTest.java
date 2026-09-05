package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.usecase.item.FindItemById;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetItemDetailsToolTest {

    @Mock
    private FindItemById findItemById;

    @Test
    void shouldReturnItemScopedToTheUnit() {
        UUID id = UUID.randomUUID();
        when(findItemById.execute(Fixtures.UNIT, id)).thenReturn(Fixtures.item(id, Fixtures.UNIT));

        Item result = new GetItemDetailsTool(findItemById).getItemDetails(Fixtures.UNIT, id);

        assertEquals(id, result.getId());
        verify(findItemById).execute(Fixtures.UNIT, id);
    }

    @Test
    void shouldRejectMissingUnitIdInsteadOfFallingBackToAGlobalRead() {
        GetItemDetailsTool tool = new GetItemDetailsTool(findItemById);
        UUID id = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> tool.getItemDetails(null, id));
        verifyNoInteractions(findItemById);
    }
}
