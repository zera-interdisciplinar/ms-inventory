package com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper;

import java.util.HashSet;

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
        Item item = new Item(node.getId(), new Barcode(node.getBarcode()), node.getStatus(), node.getUnitId(),
                modelMapper.toDomain(node.getModel()),
                node.getCreatedAt(), node.getUpdatedAt(), node.getLastEventAt(), node.getPredictedFailureDate(),
                node.getManufacturingYear(), node.getUsageIntensity(), node.getSerialNumber(), node.getAcquiredAt());
        item.restoreRegistration(node.getName(), node.getCondition(), node.getHasDamages(), node.getDamages(),
                node.getNotes(), node.getCreatedBy(), node.getCreatedByName());
        item.restorePredictionUpdatedAt(node.getPredictionUpdatedAt());
        return item;
    }
    
    public ItemNode toNode(Item item) {
        if (item == null) {
            return null;
        }
        ItemNode node = new ItemNode(item.getId(), item.getBarcode().getValue(), item.getStatus(), item.getUnitId(),
                item.getCreatedAt(), item.getUpdatedAt(), item.getLastEventAt(), item.getPredictedFailureDate(),
                item.getManufacturingYear(), item.getUsageIntensity(), item.getSerialNumber(), item.getAcquiredAt());
        node.setName(item.getName());
        node.setCondition(item.getCondition());
        node.setHasDamages(item.getHasDamages());
        node.setDamages(new HashSet<>(item.getDamages()));
        node.setNotes(item.getNotes());
        node.setCreatedBy(item.getCreatedBy());
        node.setCreatedByName(item.getCreatedByName());
        node.setPredictionUpdatedAt(item.getPredictionUpdatedAt());
        return node;
    }
}
