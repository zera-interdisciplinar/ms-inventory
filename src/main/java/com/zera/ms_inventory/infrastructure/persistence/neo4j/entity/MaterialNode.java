package com.zera.ms_inventory.infrastructure.persistence.neo4j.entity;

import java.util.UUID;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import com.zera.ms_inventory.core.domain.valueobject.MaterialCode;

@Node("Material")
public class MaterialNode {

    @Id
    private UUID id;

    private MaterialCode code;

    private String name;

    private boolean recyclable;

    private boolean hazardous;

    private String disposalGuide;

    public MaterialNode() {
    }

    public MaterialNode(UUID id, MaterialCode code, String name, boolean recyclable, boolean hazardous,
                        String disposalGuide) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.recyclable = recyclable;
        this.hazardous = hazardous;
        this.disposalGuide = disposalGuide;
    }

    public UUID getId() {
        return id;
    }

    public MaterialCode getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public boolean isRecyclable() {
        return recyclable;
    }

    public boolean isHazardous() {
        return hazardous;
    }

    public String getDisposalGuide() {
        return disposalGuide;
    }
}
