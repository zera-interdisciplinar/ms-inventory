package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.usecase.model.FindAllModels;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;

@Component
public class HazmatInventoryTool {

    private final FindAllModels findAllModels;

    public HazmatInventoryTool(FindAllModels findAllModels) {
        this.findAllModels = findAllModels;
    }

    @McpTool(
        name = "hazmat_inventory",
        description = "Audit inventory for hazardous materials - lists all models containing hazmat and their details",
        annotations = @McpTool.McpAnnotations(
            readOnlyHint = true,
            title = "Hazmat Inventory"
        )
    )
    public List<HazmatModel> getHazmatInventory(
            @McpToolParam(description = McpToolScope.UNIT_ID_DESCRIPTION, required = true) UUID unitId,
            @McpToolParam(description = "Maximum number of results", required = false) Integer limit,
            @McpToolParam(description = "Pagination offset", required = false) Integer offset) {

        int actualLimit = limit != null ? limit : 100;
        int actualOffset = offset != null ? offset : 0;

        List<Model> models = findAllModels.execute(McpToolScope.require(unitId));

        return models.stream()
            .filter(model -> model.getHazardousMaterials() != null && !model.getHazardousMaterials().isEmpty())
            .skip(actualOffset)
            .limit(actualLimit)
            .map(model -> new HazmatModel(
                model.getId(),
                model.getName(),
                model.getManufacturer(),
                model.getCategory() != null ? model.getCategory().getName() : null,
                model.getHazardousMaterials()
            ))
            .collect(Collectors.toList());
    }

    public static class HazmatModel {
        public final UUID modelId;
        public final String modelName;
        public final String manufacturer;
        public final String categoryName;
        public final Set<String> hazardousMaterials;

        public HazmatModel(UUID modelId, String modelName, String manufacturer, String categoryName,
                            Set<String> hazardousMaterials) {
            this.modelId = modelId;
            this.modelName = modelName;
            this.manufacturer = manufacturer;
            this.categoryName = categoryName;
            this.hazardousMaterials = hazardousMaterials;
        }
    }
}
