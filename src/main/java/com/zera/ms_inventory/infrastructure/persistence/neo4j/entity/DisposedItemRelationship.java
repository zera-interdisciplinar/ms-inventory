package com.zera.ms_inventory.infrastructure.persistence.neo4j.entity;

import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

/** Aresta {@code (:Disposal)-[:INCLUDES {weightKg}]->(:Item)} com o peso congelado. */
@RelationshipProperties
public class DisposedItemRelationship {

    @RelationshipId
    private String id;

    private Double weightKg;

    @TargetNode
    private ItemNode item;

    public DisposedItemRelationship() {
    }

    public DisposedItemRelationship(ItemNode item, Double weightKg) {
        this.item = item;
        this.weightKg = weightKg;
    }

    public Double getWeightKg() {
        return weightKg;
    }

    public ItemNode getItem() {
        return item;
    }
}
