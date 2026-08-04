package com.zera.ms_inventory.core.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;

public class Item {
    private final UUID id;
    private final Barcode barcode;
    private ItemStatus status;
    private UUID unitId;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastEventAt;
    private String serialNumber;
    private LocalDate acquiredAt;

    public Item(UUID id, Barcode barcode, ItemStatus status, UUID unitId, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime lastEventAt, String serialNumber, LocalDate acquiredAt) {
        this.id = id;
        this.barcode = barcode;
        this.status = status;
        this.unitId = unitId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastEventAt = lastEventAt;
        this.serialNumber = serialNumber;
        this.acquiredAt = acquiredAt;
    }

    public Item(UUID id, Barcode barcode, ItemStatus status, UUID unitId, LocalDateTime lastEventAt, String serialNumber, LocalDate acquiredAt) {
        this.id = id;
        this.barcode = barcode;
        this.status = status;
        this.unitId = unitId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastEventAt = lastEventAt;
        this.serialNumber = serialNumber;
        this.acquiredAt = acquiredAt;
    }

    public Item(UUID id, Barcode barcode, ItemStatus status, UUID unitId, String serialNumber, LocalDate acquiredAt) {
        this.id = id;
        this.barcode = barcode;
        this.status = status;
        this.unitId = unitId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastEventAt = LocalDateTime.now();
        this.serialNumber = serialNumber;
        this.acquiredAt = acquiredAt;
    }

    // -------------------------------

    public UUID getId() {
        return id;
    }

    public Barcode getBarcode() {
        return barcode;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public UUID getUnitId() {
        return unitId;
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

    public String getSerialNumber() {
        return serialNumber;
    }

    public LocalDate getAcquiredAt() {
        return acquiredAt;
    }

    // -----------------------------

    public void updateStatus(ItemStatus status) {
        this.status = status;
        touch();
    }

    public void assignUnit(UUID unitId) {
        this.unitId = unitId;
        touch();
    }

    public void updateSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
        touch();
    }

    public void updateAcquiredAt(LocalDate acquiredAt) {
        this.acquiredAt = acquiredAt;
        touch();
    }

    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    private void touchLastEvent() {
        this.lastEventAt = LocalDateTime.now();
    }
    // TODO: verify necessity of this method  
}
