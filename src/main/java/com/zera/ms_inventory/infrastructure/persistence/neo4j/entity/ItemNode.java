package com.zera.ms_inventory.infrastructure.persistence.neo4j.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;

@Node("Item")
public class ItemNode {

    @Id
    private UUID id;

    private String barcode;

    private ItemStatus status;

    private UUID unitId;

    @Relationship(type = "IS_MODEL", direction = Relationship.Direction.OUTGOING)
    private ModelNode model;

    @Property("createdAt")
    private LocalDateTime createdAt;

    @Property("updatedAt")
    private LocalDateTime updatedAt;

    @Property("lastEventAt")
    private LocalDateTime lastEventAt;

    @Property("nextPredictionDate")
    private LocalDateTime nextPredictionDate;

    private Integer manufacturingDate;

    private Integer usageIntensity;

    private String serialNumber;

    private LocalDate acquiredAt;

    public ItemNode() {
    }

    public ItemNode(UUID id, String barcode, ItemStatus status, UUID unitId, LocalDateTime createdAt,
                     LocalDateTime updatedAt, LocalDateTime lastEventAt, LocalDateTime nextPredictionDate,
                     Integer manufacturingDate, Integer usageIntensity, String serialNumber, LocalDate acquiredAt) {
        this.id = id;
        this.barcode = barcode;
        this.status = status;
        this.unitId = unitId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastEventAt = lastEventAt;
        this.nextPredictionDate = nextPredictionDate;
        this.manufacturingDate = manufacturingDate;
        this.usageIntensity = usageIntensity;
        this.serialNumber = serialNumber;
        this.acquiredAt = acquiredAt;
    }

    public UUID getId() {
        return id;
    }

    public String getBarcode() {
        return barcode;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public UUID getUnitId() {
        return unitId;
    }

    public ModelNode getModel() {
        return model;
    }

    public void setModel(ModelNode model) {
        this.model = model;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getLastEventAt() {
        return lastEventAt;
    }

    public LocalDateTime getNextPredictionDate() {
        return nextPredictionDate;
    }

    public Integer getManufacturingDate() {
        return manufacturingDate;
    }

    public Integer getUsageIntensity() {
        return usageIntensity;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public LocalDate getAcquiredAt() {
        return acquiredAt;
    }
}
