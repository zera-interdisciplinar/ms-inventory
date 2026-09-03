package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.usecase.model.FindModelById;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetModelDetailsToolTest {

    @Mock
    private FindModelById findModelById;

    @Test
    void shouldReturnModelScopedToTheUnit() {
        UUID id = UUID.randomUUID();
        when(findModelById.execute(Fixtures.UNIT, id)).thenReturn(Fixtures.model(id, Fixtures.UNIT));

        Model result = new GetModelDetailsTool(findModelById).getModelDetails(Fixtures.UNIT, id);

        assertEquals(id, result.getId());
        verify(findModelById).execute(Fixtures.UNIT, id);
    }

    @Test
    void shouldRejectMissingUnitIdInsteadOfFallingBackToAGlobalRead() {
        GetModelDetailsTool tool = new GetModelDetailsTool(findModelById);
        UUID id = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> tool.getModelDetails(null, id));
        verifyNoInteractions(findModelById);
    }
}
