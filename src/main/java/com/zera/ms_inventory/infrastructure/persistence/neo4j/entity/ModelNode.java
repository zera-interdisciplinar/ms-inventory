package com.zera.ms_inventory.infrastructure.persistence.neo4j.entity;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node("Model")
public class ModelNode {

    @Id
    private UUID id;

    private String name;

    private String manufacturer;

    private Integer warrantyMonths;

    private Integer expectedLifespanMonths;

    private Set<String> hazardousMaterials;

    @Property("createdAt")
    private LocalDateTime createdAt;

    @Property("updatedAt")
    private LocalDateTime updatedAt;

    public ModelNode() {
    }

    public ModelNode(UUID id, String name, String manufacturer, Integer warrantyMonths, Integer expectedLifespanMonths,
                      Set<String> hazardousMaterials, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.manufacturer = manufacturer;
        this.warrantyMonths = warrantyMonths;
        this.expectedLifespanMonths = expectedLifespanMonths;
        this.hazardousMaterials = hazardousMaterials;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public Integer getWarrantyMonths() {
        return warrantyMonths;
    }

    public Integer getExpectedLifespanMonths() {
        return expectedLifespanMonths;
    }

    public Set<String> getHazardousMaterials() {
        return hazardousMaterials;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
