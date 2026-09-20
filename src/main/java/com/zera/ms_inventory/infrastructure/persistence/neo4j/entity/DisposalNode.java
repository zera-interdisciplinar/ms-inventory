package com.zera.ms_inventory.infrastructure.persistence.neo4j.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import com.zera.ms_inventory.core.domain.valueobject.DestinationType;

@Node("Disposal")
public class DisposalNode {

    @Id
    private UUID id;

    private UUID unitId;

    private DestinationType destination;

    private String placeId;

    private String placeName;

    private LocalDate disposedAt;

    private String notes;

    private UUID createdBy;

    private String createdByName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /** O peso vai na relacao, congelado no momento do descarte. */
    @Relationship(type = "INCLUDES", direction = Relationship.Direction.OUTGOING)
    private Set<DisposedItemRelationship> items = new HashSet<>();

    public DisposalNode() {
    }

    public DisposalNode(UUID id, UUID unitId, DestinationType destination, String placeId, String placeName,
                        LocalDate disposedAt, String notes, UUID createdBy, String createdByName,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.unitId = unitId;
        this.destination = destination;
        this.placeId = placeId;
        this.placeName = placeName;
        this.disposedAt = disposedAt;
        this.notes = notes;
        this.createdBy = createdBy;
        this.createdByName = createdByName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUnitId() {
        return unitId;
    }

    public DestinationType getDestination() {
        return destination;
    }

    public void setDestination(DestinationType destination) {
        this.destination = destination;
    }

    public String getPlaceId() {
        return placeId;
    }

    public String getPlaceName() {
        return placeName;
    }

    public LocalDate getDisposedAt() {
        return disposedAt;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<DisposedItemRelationship> getItems() {
        return items;
    }

    public void setItems(Set<DisposedItemRelationship> items) {
        this.items = items == null ? new HashSet<>() : items;
    }
}
