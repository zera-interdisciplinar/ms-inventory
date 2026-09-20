package com.zera.ms_inventory.infrastructure.persistence.neo4j.entity;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.neo4j.core.schema.DynamicLabels;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import com.zera.ms_inventory.core.domain.valueobject.EventType;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;

/**
 * Historico do item. O item nao mapeia a relacao de volta: carregar um item nunca deve trazer o
 * historico junto. A ligacao {@code (:Item)-[:HAS_EVENT]->(:Event)} e criada pelo repositorio.
 */
@Node("Event")
public class EventNode {

    @Id
    private UUID id;

    /** Rotulo extra com o tipo, para o grafo ficar {@code :Event:APPROVED}. */
    @DynamicLabels
    private Set<String> typeLabels;

    private UUID itemId;

    private UUID unitId;

    private EventType type;

    private ItemStatus fromStatus;

    private ItemStatus toStatus;

    private String reason;

    private UUID actorId;

    private String actorName;

    private LocalDateTime occurredAt;

    public EventNode() {
    }

    public EventNode(UUID id, UUID itemId, UUID unitId, EventType type, ItemStatus fromStatus, ItemStatus toStatus,
                     String reason, UUID actorId, String actorName, LocalDateTime occurredAt) {
        this.id = id;
        this.itemId = itemId;
        this.unitId = unitId;
        this.type = type;
        this.typeLabels = type != null ? Set.of(type.name()) : Set.of();
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.reason = reason;
        this.actorId = actorId;
        this.actorName = actorName;
        this.occurredAt = occurredAt;
    }

    public UUID getId() {
        return id;
    }

    public Set<String> getTypeLabels() {
        return typeLabels;
    }

    public void setTypeLabels(Set<String> typeLabels) {
        this.typeLabels = typeLabels;
    }

    public UUID getItemId() {
        return itemId;
    }

    public UUID getUnitId() {
        return unitId;
    }

    public EventType getType() {
        return type;
    }

    public ItemStatus getFromStatus() {
        return fromStatus;
    }

    public ItemStatus getToStatus() {
        return toStatus;
    }

    public String getReason() {
        return reason;
    }

    public UUID getActorId() {
        return actorId;
    }

    public String getActorName() {
        return actorName;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
