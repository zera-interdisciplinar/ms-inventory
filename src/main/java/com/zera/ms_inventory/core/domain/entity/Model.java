package com.zera.ms_inventory.core.domain.entity;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public class Model {

    private final UUID id;
    private String name;
    private String manufacturer;
    private Integer warrantyMonths;
    private Integer expectedLifespanMonths;
    private Set<String> hazardousMaterials;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Model(UUID id, String name, String manufacturer, Integer warrantyMonths, Integer expectedLifespanMonths, Set<String> hazardousMaterials, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.manufacturer = manufacturer;
        this.warrantyMonths = warrantyMonths;
        this.expectedLifespanMonths = expectedLifespanMonths;
        this.hazardousMaterials = hazardousMaterials;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Model(UUID id, String name, String manufacturer, Integer warrantyMonths, Integer expectedLifespanMonths, Set<String> hazardousMaterials) {
        this.id = id;
        this.name = name;
        this.manufacturer = manufacturer;
        this.warrantyMonths = warrantyMonths;
        this.expectedLifespanMonths = expectedLifespanMonths;
        this.hazardousMaterials = hazardousMaterials;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // -------------------------------------------------

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
}
