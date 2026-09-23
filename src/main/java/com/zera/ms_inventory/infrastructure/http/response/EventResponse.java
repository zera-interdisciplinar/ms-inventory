package com.zera.ms_inventory.infrastructure.http.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Event;
import com.zera.ms_inventory.core.domain.valueobject.EventType;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;

public record EventResponse(
        UUID id,
        UUID itemId,
        EventType type,
        ItemStatus fromStatus,
        ItemStatus toStatus,
        String reason,
        UUID actorId,
        String actorName,
        LocalDateTime occurredAt
) {
    public static EventResponse from(Event event) {
        if (event == null) {
            return null;
        }
        return new EventResponse(event.getId(), event.getItemId(), event.getType(), event.getFromStatus(),
                event.getToStatus(), event.getReason(), event.getActorId(), event.getActorName(),
                event.getOccurredAt());
    }
}
