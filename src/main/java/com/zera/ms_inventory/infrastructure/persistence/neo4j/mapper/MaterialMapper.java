package com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper;

import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.Material;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.MaterialNode;

@Component
public class MaterialMapper {

    public Material toDomain(MaterialNode node) {
        if (node == null) {
            return null;
        }
        return new Material(node.getId(), node.getCode(), node.getName(), node.isRecyclable(), node.isHazardous(),
                node.getDisposalGuide());
    }

    public MaterialNode toNode(Material material) {
        if (material == null) {
            return null;
        }
        return new MaterialNode(material.getId(), material.getCode(), material.getName(), material.isRecyclable(),
                material.isHazardous(), material.getDisposalGuide());
    }
}
