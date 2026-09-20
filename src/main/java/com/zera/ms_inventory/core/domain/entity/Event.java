package com.zera.ms_inventory.core.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.EventType;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;

/**
 * Passo do ciclo de vida do item. E imutavel: um evento registra o que aconteceu, entao nunca e
 * editado nem apagado. O autor fica desnormalizado ({@code actorName}) porque o operario nao tem
 * permissao para resolver nomes no admin-core.
 */
public class Event {

    private final UUID id;
    private final UUID itemId;
    private final UUID unitId;
    private final EventType type;
    private final ItemStatus fromStatus;
    private final ItemStatus toStatus;
    private final String reason;
    private final UUID actorId;
    private final String actorName;
    private final LocalDateTime occurredAt;

    public Event(UUID id, UUID itemId, UUID unitId, EventType type, ItemStatus fromStatus, ItemStatus toStatus,
                 String reason, UUID actorId, String actorName, LocalDateTime occurredAt) {
        if (itemId == null || unitId == null || type == null || toStatus == null) {
            throw new IllegalArgumentException("event requires itemId, unitId, type and toStatus");
        }
        this.id = id != null ? id : UUID.randomUUID();
        this.itemId = itemId;
        this.unitId = unitId;
        this.type = type;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.reason = reason != null && !reason.isBlank() ? reason.strip() : null;
        this.actorId = actorId;
        this.actorName = actorName;
        this.occurredAt = occurredAt != null ? occurredAt : LocalDateTime.now();
    }

    /** Evento novo, gerado por uma transicao do item. */
    public static Event of(UUID itemId, UUID unitId, EventType type, ItemStatus fromStatus, ItemStatus toStatus,
                           String reason, Actor actor) {
        return new Event(null, itemId, unitId, type, fromStatus, toStatus, reason,
                actor != null ? actor.userId() : null, actor != null ? actor.name() : null, null);
    }

    public UUID getId() {
        return id;
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
