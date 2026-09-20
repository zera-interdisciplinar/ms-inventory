package com.zera.ms_inventory.core.domain.entity;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.valueobject.MaterialCode;

/** Item do catalogo global de materiais. Nao pertence a unidade e e mantido por migracao. */
public class Material {
    private final UUID id;
    private final MaterialCode code;
    private final String name;
    private final boolean recyclable;
    private final boolean hazardous;
    private final String disposalGuide;

    public Material(UUID id, MaterialCode code, String name, boolean recyclable, boolean hazardous,
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
