package com.zera.ms_inventory.core.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.valueobject.EventType;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventTest {

    @Test
    void shouldGenerateIdAndTimestampWhenNotInformed() {
        Event event = Event.of(UUID.randomUUID(), Fixtures.UNIT, EventType.APPROVED, ItemStatus.PENDING_APPROVAL,
                ItemStatus.IN_STOCK, null, Fixtures.MANAGER);

        assertThat(event.getId()).isNotNull();
        assertThat(event.getOccurredAt()).isNotNull();
        assertThat(event.getActorId()).isEqualTo(Fixtures.MANAGER.userId());
        assertThat(event.getActorName()).isEqualTo("Kevin Gestor");
    }

    /** O nome do autor fica no evento porque o operario nao pode resolver nomes no admin-core. */
    @Test
    void shouldKeepTheInformedIdentityAndMoment() {
        UUID id = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        LocalDateTime moment = LocalDateTime.of(2026, 9, 20, 10, 30);

        Event event = new Event(id, itemId, Fixtures.UNIT, EventType.REJECTED, ItemStatus.PENDING_APPROVAL,
                ItemStatus.REJECTED, "Foto ilegivel", Fixtures.MANAGER.userId(), "Kevin Gestor", moment);

        assertThat(event.getId()).isEqualTo(id);
        assertThat(event.getItemId()).isEqualTo(itemId);
        assertThat(event.getReason()).isEqualTo("Foto ilegivel");
        assertThat(event.getOccurredAt()).isEqualTo(moment);
    }

    @Test
    void shouldNormalizeABlankReasonToNull() {
        Event event = Event.of(UUID.randomUUID(), Fixtures.UNIT, EventType.CREATED, null, ItemStatus.DRAFT,
                "   ", Fixtures.OPERATOR);

        assertThat(event.getReason()).isNull();
    }

    @Test
    void shouldTrimTheReason() {
        Event event = Event.of(UUID.randomUUID(), Fixtures.UNIT, EventType.REJECTED, ItemStatus.PENDING_APPROVAL,
                ItemStatus.REJECTED, "  faltou a foto  ", Fixtures.MANAGER);

        assertThat(event.getReason()).isEqualTo("faltou a foto");
    }

    @Test
    void shouldAcceptAnEventWithoutAnActor() {
        Event event = Event.of(UUID.randomUUID(), Fixtures.UNIT, EventType.STATUS_CHANGED, ItemStatus.IN_STOCK,
                ItemStatus.IN_MAINTENANCE, null, null);

        assertThat(event.getActorId()).isNull();
        assertThat(event.getActorName()).isNull();
    }

    @Test
    void shouldRequireItemUnitTypeAndTargetStatus() {
        UUID itemId = UUID.randomUUID();
        assertThatThrownBy(() -> new Event(null, null, Fixtures.UNIT, EventType.CREATED, null, ItemStatus.DRAFT,
                null, null, null, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Event(null, itemId, null, EventType.CREATED, null, ItemStatus.DRAFT,
                null, null, null, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Event(null, itemId, Fixtures.UNIT, null, null, ItemStatus.DRAFT,
                null, null, null, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Event(null, itemId, Fixtures.UNIT, EventType.CREATED, null, null,
                null, null, null, null)).isInstanceOf(IllegalArgumentException.class);
    }
}
