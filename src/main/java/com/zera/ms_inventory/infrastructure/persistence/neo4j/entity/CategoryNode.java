package com.zera.ms_inventory.infrastructure.persistence.neo4j.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node("Category")
public class CategoryNode {

    @Id
    private UUID id;

    private UUID unitId;

    private String name;

    private String description;

    private float[] embedding;

    private String embeddedText;

    @Property("createdAt")
    private LocalDateTime createdAt;

    @Property("updatedAt")
    private LocalDateTime updatedAt;

    public CategoryNode() {
    }

    public CategoryNode(UUID id, UUID unitId, String name, String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.unitId = unitId;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUnitId() {
        return unitId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }

    public String getEmbeddedText() {
        return embeddedText;
    }

    public void setEmbeddedText(String embeddedText) {
        this.embeddedText = embeddedText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
