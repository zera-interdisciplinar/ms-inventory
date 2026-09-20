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
    private String displayCode;
    private ItemStatus status;
    private UUID unitId;
    private final Model model;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastEventAt;
    private LocalDate predictedFailureDate;
    private LocalDateTime predictionUpdatedAt;
    private Integer manufacturingYear;
    private Integer usageIntensity;
    private String serialNumber;
    private LocalDate acquiredAt;
    private String name;
    private ItemCondition condition;
    private Boolean hasDamages;
    private Set<DamageType> damages = Set.of();
    private String notes;
    private String photoKey;
    private UUID createdBy;
    private String createdByName;

    public Item(UUID id, Barcode barcode, ItemStatus status, UUID unitId, Model model, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime lastEventAt, LocalDate predictedFailureDate, Integer manufacturingYear, Integer usageIntensity, String serialNumber, LocalDate acquiredAt) {
        this.id = id;
        this.barcode = barcode;
        this.status = status;
        this.unitId = unitId;
        this.model = model;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastEventAt = lastEventAt;
        validateManufacturingYear(manufacturingYear);
        validateUsageIntensity(usageIntensity);
        this.predictedFailureDate = predictedFailureDate;
        this.manufacturingYear = manufacturingYear;
        this.usageIntensity = usageIntensity;
        this.serialNumber = serialNumber;
        this.acquiredAt = acquiredAt;
    }

    public Item(UUID id, Barcode barcode, ItemStatus status, UUID unitId, Model model, LocalDateTime lastEventAt, LocalDate predictedFailureDate, Integer manufacturingYear, Integer usageIntensity, String serialNumber, LocalDate acquiredAt) {
        this.id = id;
        this.barcode = barcode;
        this.status = status;
        this.unitId = unitId;
        this.model = model;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastEventAt = lastEventAt;
        validateManufacturingYear(manufacturingYear);
        validateUsageIntensity(usageIntensity);
        this.predictedFailureDate = predictedFailureDate;
        this.manufacturingYear = manufacturingYear;
        this.usageIntensity = usageIntensity;
        this.serialNumber = serialNumber;
        this.acquiredAt = acquiredAt;
    }

    public Item(UUID id, Barcode barcode, ItemStatus status, UUID unitId, Model model, LocalDate predictedFailureDate, Integer manufacturingYear, Integer usageIntensity, String serialNumber, LocalDate acquiredAt) {
        this.id = id;
        this.barcode = barcode;
        this.status = status;
        this.unitId = unitId;
        this.model = model;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastEventAt = LocalDateTime.now();
        validateManufacturingYear(manufacturingYear);
        validateUsageIntensity(usageIntensity);
        this.predictedFailureDate = predictedFailureDate;
        this.manufacturingYear = manufacturingYear;
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

    /** Codigo curto de 6 digitos exibido no app ("ID 265964"), unico dentro da unidade. */
    public String getDisplayCode() {
        return displayCode;
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

    /** Data prevista de quebra calculada pelo sistema preditivo. */
    public LocalDate getPredictedFailureDate() {
        return predictedFailureDate;
    }

    public LocalDateTime getPredictionUpdatedAt() {
        return predictionUpdatedAt;
    }

    public Integer getManufacturingYear() {
        return manufacturingYear;
    }

    /** Intensidade de uso na escala de 0 a 10. */
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

    /** Chave da foto no armazenamento; a URL para exibir e gerada na resposta. */
    public String getPhotoKey() {
        return photoKey;
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

    public void attachPhoto(String photoKey) {
        if (photoKey == null || photoKey.isBlank()) {
            throw new IllegalArgumentException("photoKey is required");
        }
        this.photoKey = photoKey;
        touch();
    }

    /** Reidrata a chave da foto salva. Uso exclusivo da camada de persistencia. */
    public void restorePhotoKey(String photoKey) {
        this.photoKey = photoKey;
    }

    /** O codigo e atribuido uma unica vez, no cadastro, e nao muda depois. */
    public void assignDisplayCode(String displayCode) {
        if (this.displayCode != null) {
            throw new IllegalStateException("Item " + id + " already has a display code");
        }
        if (displayCode == null || !displayCode.matches("\\d{6}")) {
            throw new IllegalArgumentException("displayCode must have exactly 6 digits");
        }
        this.displayCode = displayCode;
    }

    /** Reidrata o codigo salvo. Uso exclusivo da camada de persistencia. */
    public void restoreDisplayCode(String displayCode) {
        this.displayCode = displayCode;
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

    /** Grava o resultado do sistema preditivo e quando ele foi calculado. */
    public void recordPrediction(LocalDate predictedFailureDate) {
        this.predictedFailureDate = predictedFailureDate;
        this.predictionUpdatedAt = LocalDateTime.now();
        touch();
    }

    /** Reidrata o momento da ultima previsao. Uso exclusivo da camada de persistencia. */
    public void restorePredictionUpdatedAt(LocalDateTime predictionUpdatedAt) {
        this.predictionUpdatedAt = predictionUpdatedAt;
    }

    public void updateManufacturingYear(Integer manufacturingYear) {
        validateManufacturingYear(manufacturingYear);
        this.manufacturingYear = manufacturingYear;
        touch();
    }

    public void updateUsageIntensity(Integer usageIntensity) {
        validateUsageIntensity(usageIntensity);
        this.usageIntensity = usageIntensity;
        touch();
    }

    /** Escala de 0 a 10 informada no cadastro; e o formato que o sistema preditivo consome. */
    private static void validateUsageIntensity(Integer usageIntensity) {
        if (usageIntensity != null && (usageIntensity < 0 || usageIntensity > 10)) {
            throw new IllegalArgumentException("usageIntensity must be between 0 and 10");
        }
    }

    private static void validateManufacturingYear(Integer year) {
        if (year != null && (year < 1950 || year > LocalDate.now().getYear())) {
            throw new IllegalArgumentException("manufacturingYear must be between 1950 and the current year");
        }
    }

    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    private void touchLastEvent() {
        this.lastEventAt = LocalDateTime.now();
    }
    // TODO: verify necessity of this method  
}
