package com.zera.ms_inventory.core.domain.entity;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Set;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.ApprovalStatus;

public class Model {

    private final UUID id;
    private final UUID unitId;
    private String name;
    private String manufacturer;
    private Integer warrantyMonths;
    private Integer expectedLifespanMonths;
    private Set<Material> materials;
    private Double estimatedWeightKg;
    private String notes;
    private final Category category;
    // modelos anteriores ao fluxo de aprovacao eram criados so por gestores
    private ApprovalStatus approvalStatus = ApprovalStatus.APPROVED;
    private String rejectionReason;
    private UUID createdBy;
    private UUID reviewedBy;
    private LocalDateTime reviewedAt;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Model(UUID id, UUID unitId, String name, String manufacturer, Integer warrantyMonths,
                 Integer expectedLifespanMonths, Set<Material> materials, Double estimatedWeightKg, String notes,
                 Category category, LocalDateTime createdAt, LocalDateTime updatedAt) {
        validateWeight(estimatedWeightKg);
        this.id = id;
        this.unitId = unitId;
        this.name = name;
        this.manufacturer = manufacturer;
        this.warrantyMonths = warrantyMonths;
        this.expectedLifespanMonths = expectedLifespanMonths;
        this.materials = materials == null ? Set.of() : Set.copyOf(materials);
        this.estimatedWeightKg = estimatedWeightKg;
        this.notes = notes;
        this.category = category;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Model(UUID id, UUID unitId, String name, String manufacturer, Integer warrantyMonths,
                 Integer expectedLifespanMonths, Set<Material> materials, Double estimatedWeightKg, String notes,
                 Category category) {
        this(id, unitId, name, manufacturer, warrantyMonths, expectedLifespanMonths, materials, estimatedWeightKg,
                notes, category, LocalDateTime.now(), LocalDateTime.now());
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

    public Set<Material> getMaterials() {
        return materials;
    }

    public Double getEstimatedWeightKg() {
        return estimatedWeightKg;
    }

    public String getNotes() {
        return notes;
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

    public ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public UUID getReviewedBy() {
        return reviewedBy;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    /** Perigoso quando qualquer material do catalogo que compoe o modelo e perigoso. */
    public boolean isHazardous() {
        return materials.stream().anyMatch(Material::isHazardous);
    }

    // -------------------------------------

    /**
     * Cadastro de um modelo novo: o do operario nasce pendente de aprovacao; o do gestor ja nasce
     * aprovado, revisado por ele mesmo.
     */
    public void registerBy(Actor actor) {
        this.createdBy = actor.userId();
        this.rejectionReason = null;
        if (actor.isManager()) {
            this.approvalStatus = ApprovalStatus.APPROVED;
            this.reviewedBy = actor.userId();
            this.reviewedAt = LocalDateTime.now();
        } else {
            this.approvalStatus = ApprovalStatus.PENDING;
            this.reviewedBy = null;
            this.reviewedAt = null;
        }
        touch();
    }

    /** Reidrata o estado de aprovacao salvo. Uso exclusivo da camada de persistencia. */
    public void restoreApproval(ApprovalStatus approvalStatus, String rejectionReason, UUID createdBy,
                                UUID reviewedBy, LocalDateTime reviewedAt) {
        this.approvalStatus = approvalStatus == null ? ApprovalStatus.APPROVED : approvalStatus;
        this.rejectionReason = rejectionReason;
        this.createdBy = createdBy;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = reviewedAt;
    }

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

    public void changeMaterials(Set<Material> newMaterials) {
        this.materials = newMaterials == null ? Set.of() : Set.copyOf(newMaterials);
        touch();
    }

    public void changeEstimatedWeightKg(Double newEstimatedWeightKg) {
        validateWeight(newEstimatedWeightKg);
        this.estimatedWeightKg = newEstimatedWeightKg;
        touch();
    }

    public void changeNotes(String newNotes) {
        this.notes = newNotes;
        touch();
    }

    /** Texto que alimenta o embedding. Muda aqui, muda o vetor no proximo save. */
    public String toEmbeddableText() {
        StringBuilder sb = new StringBuilder();
        if (name != null) sb.append(name).append(' ');
        if (manufacturer != null) sb.append(manufacturer).append(' ');
        if (category != null && category.getName() != null) sb.append(category.getName()).append(' ');
        // ordenado: Set nao tem ordem estavel e o texto instavel re-embedaria a cada save
        materials.stream().map(Material::getName).sorted(Comparator.naturalOrder())
                .forEach(m -> sb.append(m).append(' '));
        return sb.toString().trim();
    }

    private static void validateWeight(Double weightKg) {
        if (weightKg != null && weightKg <= 0) {
            throw new IllegalArgumentException("estimatedWeightKg must be greater than 0");
        }
    }
}
