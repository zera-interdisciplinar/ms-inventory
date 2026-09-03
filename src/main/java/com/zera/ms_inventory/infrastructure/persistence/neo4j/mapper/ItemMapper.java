package com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper;

import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.ItemNode;

@Component
public class ItemMapper {

    private final ModelMapper modelMapper;

    public ItemMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public Item toDomain(ItemNode node) {
        if (node == null) {
            return null;
        }
        return new Item(node.getId(), new Barcode(node.getBarcode()), node.getStatus(), node.getUnitId(),
                modelMapper.toDomain(node.getModel()),
                node.getCreatedAt(), node.getUpdatedAt(), node.getLastEventAt(), node.getNextPredictionDate(),
                node.getManufacturingDate(), node.getUsageIntensity(), node.getSerialNumber(), node.getAcquiredAt());
    }
    
    public ItemNode toNode(Item item) {
        if (item == null) {
            return null;
        }
        return new ItemNode(item.getId(), item.getBarcode().getValue(), item.getStatus(), item.getUnitId(),
                item.getCreatedAt(), item.getUpdatedAt(), item.getLastEventAt(), item.getNextPredictionDate(),
                item.getManufacturingDate(), item.getUsageIntensity(), item.getSerialNumber(), item.getAcquiredAt());
    }
}
