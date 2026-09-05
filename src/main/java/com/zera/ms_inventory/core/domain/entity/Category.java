package com.zera.ms_inventory.core.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public class Category {
    private final UUID id;
    private final UUID unitId;
    private String name;
    private String description;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Category(UUID id, UUID unitId, String name, String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.unitId = unitId;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Category(UUID id, UUID unitId, String name, String description) {
        this.id = id;
        this.unitId = unitId;
        this.name = name;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ----------------------

    public void touch(){
        this.updatedAt = LocalDateTime.now();
    }

    public void rename(String name){
        this.name = name;
        touch();
    }

    public void updateDescription(String description){
        this.description = description;
        touch();
    }

    // --------------------

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /** Texto que alimenta o embedding. Muda aqui, muda o vetor no proximo save. */
    public String toEmbeddableText() {
        return String.join(" ", name == null ? "" : name, description == null ? "" : description).trim();
    }
}
