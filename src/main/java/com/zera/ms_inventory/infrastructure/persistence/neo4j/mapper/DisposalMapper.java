package com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.Disposal;
import com.zera.ms_inventory.core.domain.valueobject.DisposedItem;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.DisposalNode;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.DisposedItemRelationship;

@Component
public class DisposalMapper {

    public Disposal toDomain(DisposalNode node) {
        if (node == null) {
            return null;
        }
        List<DisposedItem> items = node.getItems().stream()
                .filter(relationship -> relationship.getItem() != null)
                .map(relationship -> new DisposedItem(relationship.getItem().getId(),
                        relationship.getItem().getDisplayCode(), relationship.getItem().getName(),
                        relationship.getWeightKg()))
                // ordem estavel: o grafo devolve as arestas sem garantia de ordem
                .sorted(java.util.Comparator.comparing(item -> String.valueOf(item.displayCode())))
                .toList();
        return new Disposal(node.getId(), node.getUnitId(), node.getDestination(), node.getPlaceId(),
                node.getPlaceName(), node.getDisposedAt(), node.getNotes(), items, node.getCreatedBy(),
                node.getCreatedByName(), node.getCreatedAt(), node.getUpdatedAt());
    }

    /** As arestas com os itens sao montadas pelo repositorio, que resolve os nos gravados. */
    public DisposalNode toNode(Disposal disposal) {
        if (disposal == null) {
            return null;
        }
        return new DisposalNode(disposal.getId(), disposal.getUnitId(), disposal.getDestination(),
                disposal.getPlaceId(), disposal.getPlaceName(), disposal.getDisposedAt(), disposal.getNotes(),
                disposal.getCreatedBy(), disposal.getCreatedByName(), disposal.getCreatedAt(),
                disposal.getUpdatedAt());
    }

    public DisposedItemRelationship toRelationship(
            com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.ItemNode item, Double weightKg) {
        return new DisposedItemRelationship(item, weightKg);
    }
}
