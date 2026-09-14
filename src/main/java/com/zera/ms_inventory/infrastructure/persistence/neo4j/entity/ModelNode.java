package com.zera.ms_inventory.infrastructure.persistence.neo4j.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("Model")
public class ModelNode {

    @Id
    private UUID id;

    private UUID unitId;

    private String name;

    private String manufacturer;

    private Integer warrantyMonths;

    private Integer expectedLifespanMonths;

    /** Anexado pelo ModelRepositoryImpl com os MaterialNode ja persistidos do catalogo. */
    @Relationship(type = "MADE_OF", direction = Relationship.Direction.OUTGOING)
    private Set<MaterialNode> materials = new HashSet<>();

    private Double estimatedWeightKg;

    private String notes;

    @Relationship(type = "BELONGS_TO", direction = Relationship.Direction.OUTGOING)
    private CategoryNode category;

    /** Vetor do indice `model_embeddings`. Escrito por ModelRepositoryImpl.save. */
    private float[] embedding;

    /** Texto que gerou `embedding`; usado para nao re-embedar quando nada mudou. */
    private String embeddedText;

    @Property("createdAt")
    private LocalDateTime createdAt;

    @Property("updatedAt")
    private LocalDateTime updatedAt;

    public ModelNode() {
    }

    public ModelNode(UUID id, UUID unitId, String name, String manufacturer, Integer warrantyMonths,
                      Integer expectedLifespanMonths,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.unitId = unitId;
        this.name = name;
        this.manufacturer = manufacturer;
        this.warrantyMonths = warrantyMonths;
        this.expectedLifespanMonths = expectedLifespanMonths;
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

    public String getManufacturer() {
        return manufacturer;
    }

    public Integer getWarrantyMonths() {
        return warrantyMonths;
    }

    public Integer getExpectedLifespanMonths() {
        return expectedLifespanMonths;
    }

    public Set<MaterialNode> getMaterials() {
        return materials;
    }

    public void setMaterials(Set<MaterialNode> materials) {
        this.materials = materials;
    }

    public Double getEstimatedWeightKg() {
        return estimatedWeightKg;
    }

    public void setEstimatedWeightKg(Double estimatedWeightKg) {
        this.estimatedWeightKg = estimatedWeightKg;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public CategoryNode getCategory() {
        return category;
    }

    public void setCategory(CategoryNode category) {
        this.category = category;
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
