package com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Disposal;
import com.zera.ms_inventory.core.domain.valueobject.DestinationType;
import com.zera.ms_inventory.core.domain.valueobject.DisposedItem;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.DisposalNode;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.DisposedItemRelationship;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.ItemNode;

import static org.assertj.core.api.Assertions.assertThat;

class DisposalMapperTest {

    private final DisposalMapper mapper = new DisposalMapper();
    private final ItemMapper itemMapper = new ItemMapper(new ModelMapper(new CategoryMapper()));

    private ItemNode itemNode(String displayCode) {
        ItemNode node = itemMapper.toNode(Fixtures.item(UUID.randomUUID(), Fixtures.UNIT));
        node.setDisplayCode(displayCode);
        node.setName("Notebook " + displayCode);
        return node;
    }

    @Test
    void shouldMapTheDisposalWithItsFrozenItems() {
        DisposalNode node = new DisposalNode(UUID.randomUUID(), Fixtures.UNIT, DestinationType.RECYCLING,
                "places/abc", "Ecoponto", LocalDate.now(), "sem observacao",
                Fixtures.OPERATOR.userId(), "Gustavo Operario", LocalDateTime.now(), LocalDateTime.now());
        node.setItems(Set.of(new DisposedItemRelationship(itemNode("100002"), 1.5),
                new DisposedItemRelationship(itemNode("100001"), 2.5)));

        Disposal disposal = mapper.toDomain(node);

        assertThat(disposal.getDestination()).isEqualTo(DestinationType.RECYCLING);
        assertThat(disposal.getPlaceId()).isEqualTo("places/abc");
        assertThat(disposal.totalWeightKg()).isEqualTo(4.0);
        // ordem estavel: o grafo devolve as arestas sem garantia de ordem
        assertThat(disposal.getItems()).extracting(DisposedItem::displayCode)
                .containsExactly("100001", "100002");
    }

    @Test
    void shouldIgnoreARelationshipWithoutItem() {
        DisposalNode node = new DisposalNode(UUID.randomUUID(), Fixtures.UNIT, DestinationType.LANDFILL, null,
                null, LocalDate.now(), null, null, null, LocalDateTime.now(), LocalDateTime.now());
        node.setItems(Set.of(new DisposedItemRelationship(itemNode("100001"), 1.0),
                new DisposedItemRelationship(null, 9.0)));

        assertThat(mapper.toDomain(node).getItems()).hasSize(1);
    }

    /** As arestas sao montadas pelo repositorio com os nos ja gravados. */
    @Test
    void shouldMapDomainToNodeWithoutTheItems() {
        Disposal disposal = Disposal.register(Fixtures.UNIT, DestinationType.DONATION, "places/x", "Ponto",
                LocalDate.now(), "doado", List.of(new DisposedItem(UUID.randomUUID(), "100001", "Notebook", 2.0)),
                Fixtures.MANAGER);

        DisposalNode node = mapper.toNode(disposal);

        assertThat(node.getId()).isEqualTo(disposal.getId());
        assertThat(node.getUnitId()).isEqualTo(Fixtures.UNIT);
        assertThat(node.getDestination()).isEqualTo(DestinationType.DONATION);
        assertThat(node.getCreatedByName()).isEqualTo("Kevin Gestor");
        assertThat(node.getItems()).isEmpty();
    }

    @Test
    void shouldBuildTheRelationshipWithTheFrozenWeight() {
        DisposedItemRelationship relationship = mapper.toRelationship(itemNode("100003"), 7.5);

        assertThat(relationship.getWeightKg()).isEqualTo(7.5);
        assertThat(relationship.getItem().getDisplayCode()).isEqualTo("100003");
    }

    /** O driver instancia os nos pelo construtor vazio e preenche pelos acessores. */
    @Test
    void shouldExposeTheEmptyConstructorAndAccessorsForTheDriver() {
        DisposalNode node = new DisposalNode();
        node.setDestination(DestinationType.LANDFILL);
        node.setUpdatedAt(LocalDateTime.of(2026, 9, 20, 10, 0));
        node.setItems(null);

        assertThat(node.getId()).isNull();
        assertThat(node.getDestination()).isEqualTo(DestinationType.LANDFILL);
        assertThat(node.getUpdatedAt()).isEqualTo(LocalDateTime.of(2026, 9, 20, 10, 0));
        assertThat(node.getItems()).isEmpty();
        assertThat(node.getNotes()).isNull();
        assertThat(node.getPlaceName()).isNull();
        assertThat(node.getDisposedAt()).isNull();
        assertThat(node.getCreatedBy()).isNull();
        assertThat(node.getCreatedAt()).isNull();

        DisposedItemRelationship relationship = new DisposedItemRelationship();
        assertThat(relationship.getItem()).isNull();
        assertThat(relationship.getWeightKg()).isNull();
    }

    @Test
    void shouldReturnNullForMissingObjects() {
        assertThat(mapper.toDomain(null)).isNull();
        assertThat(mapper.toNode(null)).isNull();
    }
}
