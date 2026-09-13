package com.zera.ms_inventory.core.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.core.domain.valueobject.DamageType;
import com.zera.ms_inventory.core.domain.valueobject.ItemCondition;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;

public class Item {
    private final UUID id;
    private final Barcode barcode;
    private ItemStatus status;
    private UUID unitId;
    private final Model model;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastEventAt;
    private LocalDateTime nextPredictionDate;
    private Integer manufacturingDate;
    private Integer usageIntensity;
    private String serialNumber;
    private LocalDate acquiredAt;
    private String name;
    private ItemCondition condition;
    private Boolean hasDamages;
    private Set<DamageType> damages = Set.of();
    private String notes;
    private UUID createdBy;
    private String createdByName;

    public Item(UUID id, Barcode barcode, ItemStatus status, UUID unitId, Model model, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime lastEventAt, LocalDateTime nextPredictionDate, Integer manufacturingDate, Integer usageIntensity, String serialNumber, LocalDate acquiredAt) {
        this.id = id;
        this.barcode = barcode;
        this.status = status;
        this.unitId = unitId;
        this.model = model;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastEventAt = lastEventAt;
        this.nextPredictionDate = nextPredictionDate;
        this.manufacturingDate = manufacturingDate;
        this.usageIntensity = usageIntensity;
        this.serialNumber = serialNumber;
        this.acquiredAt = acquiredAt;
    }

    public Item(UUID id, Barcode barcode, ItemStatus status, UUID unitId, Model model, LocalDateTime lastEventAt, LocalDateTime nextPredictionDate, Integer manufacturingDate, Integer usageIntensity, String serialNumber, LocalDate acquiredAt) {
        this.id = id;
        this.barcode = barcode;
        this.status = status;
        this.unitId = unitId;
        this.model = model;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastEventAt = lastEventAt;
        this.nextPredictionDate = nextPredictionDate;
        this.manufacturingDate = manufacturingDate;
        this.usageIntensity = usageIntensity;
        this.serialNumber = serialNumber;
        this.acquiredAt = acquiredAt;
    }

    public Item(UUID id, Barcode barcode, ItemStatus status, UUID unitId, Model model, LocalDateTime nextPredictionDate, Integer manufacturingDate, Integer usageIntensity, String serialNumber, LocalDate acquiredAt) {
        this.id = id;
        this.barcode = barcode;
        this.status = status;
        this.unitId = unitId;
        this.model = model;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastEventAt = LocalDateTime.now();
        this.nextPredictionDate = nextPredictionDate;
        this.manufacturingDate = manufacturingDate;
        this.usageIntensity = usageIntensity;
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

    public Model getModel() {
        return model;
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

    public String getName() {
        return name;
    }

    public ItemCondition getCondition() {
        return condition;
    }

    /** Resposta do "Possui danos?"; nulo enquanto o cadastro nao respondeu. */
    public Boolean getHasDamages() {
        return hasDamages;
    }

    public Set<DamageType> getDamages() {
        return damages;
    }

    public String getNotes() {
        return notes;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    // -----------------------------

    /** Dados do formulario de cadastro/edicao do item. */
    public void describe(String name, ItemCondition condition, Boolean hasDamages, Set<DamageType> damages,
                         String notes) {
        applyDescription(name, condition, hasDamages, damages, notes);
        touch();
    }

    /** Guarda quem cadastrou; o nome fica gravado para o "Cadastrado por" de qualquer papel. */
    public void registerBy(Actor actor) {
        this.createdBy = actor.userId();
        this.createdByName = actor.name();
        touch();
    }

    /** Reidrata os dados de cadastro salvos. Uso exclusivo da camada de persistencia. */
    public void restoreRegistration(String name, ItemCondition condition, Boolean hasDamages,
                                    Set<DamageType> damages, String notes, UUID createdBy, String createdByName) {
        applyDescription(name, condition, hasDamages, damages, notes);
        this.createdBy = createdBy;
        this.createdByName = createdByName;
    }

    private void applyDescription(String name, ItemCondition condition, Boolean hasDamages,
                                  Set<DamageType> damages, String notes) {
        Set<DamageType> safeDamages = damages == null ? Set.of() : Set.copyOf(damages);
        if (Boolean.FALSE.equals(hasDamages) && !safeDamages.isEmpty()) {
            throw new IllegalArgumentException("damages must be empty when hasDamages is false");
        }
        this.name = name;
        this.condition = condition;
        this.hasDamages = hasDamages;
        this.damages = safeDamages;
        this.notes = notes;
    }


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

    public void updateNextPredictionDate(LocalDateTime nextPredictionDate) {
        this.nextPredictionDate = nextPredictionDate;
        touch();
    }

    public void updateManufacturingDate(Integer manufacturingDate) {
        this.manufacturingDate = manufacturingDate;
        touch();
    }

    public void updateUsageIntensity(Integer usageIntensity) {
        this.usageIntensity = usageIntensity;
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
