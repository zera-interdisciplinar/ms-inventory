package com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Event;
import com.zera.ms_inventory.core.domain.valueobject.EventType;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.EventNode;

import static org.assertj.core.api.Assertions.assertThat;

class EventMapperTest {

    private final EventMapper mapper = new EventMapper();

    @Test
    void shouldMapDomainToNodeAndBack() {
        UUID itemId = UUID.randomUUID();
        LocalDateTime moment = LocalDateTime.of(2026, 9, 20, 14, 30);
        Event event = new Event(UUID.randomUUID(), itemId, Fixtures.UNIT, EventType.REJECTED,
                ItemStatus.PENDING_APPROVAL, ItemStatus.REJECTED, "Foto ilegivel",
                Fixtures.MANAGER.userId(), "Kevin Gestor", moment);

        EventNode node = mapper.toNode(event);
        Event result = mapper.toDomain(node);

        assertThat(result.getId()).isEqualTo(event.getId());
        assertThat(result.getItemId()).isEqualTo(itemId);
        assertThat(result.getUnitId()).isEqualTo(Fixtures.UNIT);
        assertThat(result.getType()).isEqualTo(EventType.REJECTED);
        assertThat(result.getFromStatus()).isEqualTo(ItemStatus.PENDING_APPROVAL);
        assertThat(result.getToStatus()).isEqualTo(ItemStatus.REJECTED);
        assertThat(result.getReason()).isEqualTo("Foto ilegivel");
        assertThat(result.getActorId()).isEqualTo(Fixtures.MANAGER.userId());
        assertThat(result.getActorName()).isEqualTo("Kevin Gestor");
        assertThat(result.getOccurredAt()).isEqualTo(moment);
    }

    /** O tipo vira rotulo do no, para o grafo ficar {@code :Event:APPROVED}. */
    @Test
    void shouldTurnTheTypeIntoANodeLabel() {
        Event event = Event.of(UUID.randomUUID(), Fixtures.UNIT, EventType.APPROVED, ItemStatus.PENDING_APPROVAL,
                ItemStatus.IN_STOCK, null, Fixtures.MANAGER);

        assertThat(mapper.toNode(event).getTypeLabels()).containsExactly("APPROVED");
    }

    @Test
    void shouldMapTheFirstEventWithoutOriginOrReason() {
        UUID itemId = UUID.randomUUID();
        Event event = Event.of(itemId, Fixtures.UNIT, EventType.CREATED, null, ItemStatus.IN_STOCK, null,
                Fixtures.OPERATOR);

        Event result = mapper.toDomain(mapper.toNode(event));

        assertThat(result.getFromStatus()).isNull();
        assertThat(result.getReason()).isNull();
        assertThat(result.getToStatus()).isEqualTo(ItemStatus.IN_STOCK);
    }

    @Test
    void shouldKeepTheLabelsSettableForTheDriver() {
        EventNode node = new EventNode();
        node.setTypeLabels(java.util.Set.of("CREATED"));

        assertThat(node.getTypeLabels()).containsExactly("CREATED");
        assertThat(node.getId()).isNull();
    }

    @Test
    void shouldReturnNullForMissingObjects() {
        assertThat(mapper.toDomain(null)).isNull();
        assertThat(mapper.toNode(null)).isNull();
    }
}
