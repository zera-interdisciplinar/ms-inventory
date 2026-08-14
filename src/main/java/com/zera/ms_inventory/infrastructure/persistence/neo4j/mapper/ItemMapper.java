package com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper;

import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.ItemNode;

@Component
public class ItemMapper {

    public Item toDomain(ItemNode node) {
        return new Item(node.getId(), new Barcode(node.getBarcode()), node.getStatus(), node.getUnitId(),
                node.getCreatedAt(), node.getUpdatedAt(), node.getLastEventAt(), node.getNextPredictionDate(),
                node.getManufacturingDate(), node.getUsageIntensity(), node.getSerialNumber(), node.getAcquiredAt());
    }

    public ItemNode toNode(Item item) {
        return new ItemNode(item.getId(), item.getBarcode().getValue(), item.getStatus(), item.getUnitId(),
                item.getCreatedAt(), item.getUpdatedAt(), item.getLastEventAt(), item.getNextPredictionDate(),
                item.getManufacturingDate(), item.getUsageIntensity(), item.getSerialNumber(), item.getAcquiredAt());
    }
}
