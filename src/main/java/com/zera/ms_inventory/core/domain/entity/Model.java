package com.zera.ms_inventory.core.domain.entity;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public class Model {

    private final UUID id;
    private final UUID unitId;
    private String name;
    private String manufacturer;
    private Integer warrantyMonths;
    private Integer expectedLifespanMonths;
    private Set<String> hazardousMaterials;
    private final Category category;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Model(UUID id, UUID unitId, String name, String manufacturer, Integer warrantyMonths, Integer expectedLifespanMonths, Set<String> hazardousMaterials, Category category, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.unitId = unitId;
        this.name = name;
        this.manufacturer = manufacturer;
        this.warrantyMonths = warrantyMonths;
        this.expectedLifespanMonths = expectedLifespanMonths;
        this.hazardousMaterials = hazardousMaterials;
        this.category = category;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Model(UUID id, UUID unitId, String name, String manufacturer, Integer warrantyMonths, Integer expectedLifespanMonths, Set<String> hazardousMaterials, Category category) {
        this(id, unitId, name, manufacturer, warrantyMonths, expectedLifespanMonths, hazardousMaterials, category,
                LocalDateTime.now(), LocalDateTime.now());
    }

    // -------------------------------------------------

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

    public Set<String> getHazardousMaterials() {
        return hazardousMaterials;
    }

    public Category getCategory() {
        return category;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // -------------------------------------

    public void touch(){
        this.updatedAt = LocalDateTime.now();
    }

    public void rename(String newName) {
        this.name = newName;
        touch();
    }

    public void changeManufacturer(String newManufacturer) {
        this.manufacturer = newManufacturer;
        touch();
    }

    public void changeWarrantyMonths(Integer newWarrantyMonths) {
        this.warrantyMonths = newWarrantyMonths;
        touch();
    }

    public void changeExpectedLifespanMonths(Integer newExpectedLifespanMonths) {
        this.expectedLifespanMonths = newExpectedLifespanMonths;
        touch();
    }

    public void changeHazardousMaterials(Set<String> newHazardousMaterials) {
        this.hazardousMaterials = newHazardousMaterials;
        touch();
    }

    /** Texto que alimenta o embedding. Muda aqui, muda o vetor no proximo save. */
    public String toEmbeddableText() {
        StringBuilder sb = new StringBuilder();
        if (name != null) sb.append(name).append(' ');
        if (manufacturer != null) sb.append(manufacturer).append(' ');
        if (category != null && category.getName() != null) sb.append(category.getName()).append(' ');
        // ordenado: Set nao tem ordem estavel e o texto instavel re-embedaria a cada save
        if (hazardousMaterials != null) hazardousMaterials.stream().sorted().forEach(m -> sb.append(m).append(' '));
        return sb.toString().trim();
    }
}
