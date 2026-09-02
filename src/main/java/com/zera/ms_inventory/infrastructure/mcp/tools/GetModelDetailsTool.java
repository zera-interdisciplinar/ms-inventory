package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.usecase.model.FindModelById;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;

@Component
public class GetModelDetailsTool {

    private final FindModelById findModelById;

    public GetModelDetailsTool(FindModelById findModelById) {
        this.findModelById = findModelById;
    }

    @McpTool(
        name = "get_model_details",
        description = "Retrieve detailed information about a specific product model including warranty, lifespan, and hazardous materials",
        annotations = @McpTool.McpAnnotations(
            readOnlyHint = true,
            title = "Get Model Details"
        )
    )
    public Model getModelDetails(
            @McpToolParam(description = "UUID of the model to retrieve", required = true) UUID modelId) {
        return findModelById.execute(modelId);
    }
}
