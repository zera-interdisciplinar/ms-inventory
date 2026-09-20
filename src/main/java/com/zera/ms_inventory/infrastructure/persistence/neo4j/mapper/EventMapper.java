package com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper;

import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.Event;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.EventNode;

@Component
public class EventMapper {

    public Event toDomain(EventNode node) {
        if (node == null) {
            return null;
        }
        return new Event(node.getId(), node.getItemId(), node.getUnitId(), node.getType(), node.getFromStatus(),
                node.getToStatus(), node.getReason(), node.getActorId(), node.getActorName(), node.getOccurredAt());
    }

    public EventNode toNode(Event event) {
        if (event == null) {
            return null;
        }
        return new EventNode(event.getId(), event.getItemId(), event.getUnitId(), event.getType(),
                event.getFromStatus(), event.getToStatus(), event.getReason(), event.getActorId(),
                event.getActorName(), event.getOccurredAt());
    }
}
