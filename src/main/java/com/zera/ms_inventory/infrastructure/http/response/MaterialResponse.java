package com.zera.ms_inventory.infrastructure.http.response;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Material;
import com.zera.ms_inventory.core.domain.valueobject.MaterialCode;

public record MaterialResponse(
        UUID id,
        MaterialCode code,
        String name,
        boolean recyclable,
        boolean hazardous,
        String disposalGuide
) {
    public static MaterialResponse from(Material material) {
        if (material == null) {
            return null;
        }
        return new MaterialResponse(material.getId(), material.getCode(), material.getName(),
                material.isRecyclable(), material.isHazardous(), material.getDisposalGuide());
    }
}
