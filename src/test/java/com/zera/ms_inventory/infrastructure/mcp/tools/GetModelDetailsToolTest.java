package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.usecase.model.FindModelById;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetModelDetailsToolTest {

    @Mock
    private FindModelById findModelById;

    @Test
    void shouldReturnModelDetails() {
        UUID modelId = UUID.randomUUID();
        Model model = new Model(modelId, "Drill", "Acme", 12, 60, Set.of());
        when(findModelById.execute(modelId)).thenReturn(model);

        GetModelDetailsTool tool = new GetModelDetailsTool(findModelById);

        assertEquals(model, tool.getModelDetails(modelId));
    }
}
