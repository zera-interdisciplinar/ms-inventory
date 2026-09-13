package com.zera.ms_inventory.core.domain.exception;

import com.zera.ms_inventory.core.domain.valueobject.MaterialCode;

public class MaterialNotFoundException extends RuntimeException {
    public MaterialNotFoundException(MaterialCode code) {
        super("Material not found with code: " + code);
    }
}
