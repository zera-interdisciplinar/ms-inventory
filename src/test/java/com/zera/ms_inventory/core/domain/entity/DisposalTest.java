package com.zera.ms_inventory.core.domain.entity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.valueobject.DestinationType;
import com.zera.ms_inventory.core.domain.valueobject.DisposedItem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DisposalTest {

    private static DisposedItem item(double weight) {
        return new DisposedItem(UUID.randomUUID(), "100001", "Notebook", weight);
    }

    @Test
    void shouldRegisterWithDefaultsForIdDateAndAuthor() {
        Disposal disposal = Disposal.register(Fixtures.UNIT, DestinationType.RECYCLING, "places/abc",
                "Ecoponto Central", null, null, List.of(item(2.5)), Fixtures.OPERATOR);

        assertThat(disposal.getId()).isNotNull();
        assertThat(disposal.getDisposedAt()).isEqualTo(LocalDate.now());
        assertThat(disposal.getCreatedBy()).isEqualTo(Fixtures.OPERATOR.userId());
        assertThat(disposal.getCreatedByName()).isEqualTo("Gustavo Operario");
        assertThat(disposal.getUpdatedAt()).isEqualTo(disposal.getCreatedAt());
    }

    @Test
    void shouldSumTheFrozenWeightIgnoringItemsWithoutIt() {
        Disposal disposal = Disposal.register(Fixtures.UNIT, DestinationType.LANDFILL, null, null, null, null,
                List.of(item(2.5), item(1.25), new DisposedItem(UUID.randomUUID(), "100003", "Cabo", null)),
                Fixtures.MANAGER);

        assertThat(disposal.totalWeightKg()).isEqualTo(3.75);
    }

    /** O destino errado distorce a taxa de reciclagem, entao ele e corrigivel. */
    @Test
    void shouldCorrectTheDestinationAndTouchTheUpdate() {
        Disposal disposal = Disposal.register(Fixtures.UNIT, DestinationType.LANDFILL, null, null, null, null,
                List.of(item(1.0)), Fixtures.OPERATOR);

        disposal.correctDestination(DestinationType.RECYCLING);

        assertThat(disposal.getDestination()).isEqualTo(DestinationType.RECYCLING);
        assertThat(disposal.getUpdatedAt()).isAfterOrEqualTo(disposal.getCreatedAt());
    }

    @Test
    void shouldNormalizeBlankPlaceAndNotes() {
        Disposal disposal = Disposal.register(Fixtures.UNIT, DestinationType.DONATION, "  ", "  ", null, "  ",
                List.of(item(1.0)), Fixtures.OPERATOR);

        assertThat(disposal.getPlaceId()).isNull();
        assertThat(disposal.getPlaceName()).isNull();
        assertThat(disposal.getNotes()).isNull();
    }

    /** Sem agendamento no sistema, o descarte e sempre registro de algo ja feito. */
    @Test
    void shouldRejectAFutureDate() {
        List<DisposedItem> items = List.of(item(1.0));

        assertThatThrownBy(() -> Disposal.register(Fixtures.UNIT, DestinationType.RECYCLING, null, null,
                LocalDate.now().plusDays(1), null, items, Fixtures.OPERATOR))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("future");
    }

    @Test
    void shouldRequireUnitDestinationAndItems() {
        List<DisposedItem> items = List.of(item(1.0));

        assertThatThrownBy(() -> Disposal.register(null, DestinationType.RECYCLING, null, null, null, null,
                items, Fixtures.OPERATOR)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Disposal.register(Fixtures.UNIT, null, null, null, null, null, items,
                Fixtures.OPERATOR)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Disposal.register(Fixtures.UNIT, DestinationType.RECYCLING, null, null, null,
                null, List.of(), Fixtures.OPERATOR)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Disposal(null, Fixtures.UNIT, DestinationType.RECYCLING, null, null, null,
                null, null, null, null, null, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectCorrectingToNoDestination() {
        Disposal disposal = Disposal.register(Fixtures.UNIT, DestinationType.RECYCLING, null, null, null, null,
                List.of(item(1.0)), Fixtures.OPERATOR);

        assertThatThrownBy(() -> disposal.correctDestination(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectAnItemWithoutIdOrWithNegativeWeight() {
        assertThatThrownBy(() -> new DisposedItem(null, "100001", "Notebook", 1.0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DisposedItem(UUID.randomUUID(), "100001", "Notebook", -0.5))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
