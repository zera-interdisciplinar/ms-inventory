package com.zera.ms_inventory.core.domain.valueobject;

import java.util.Objects;

public class Barcode {

    private final String value;

    public Barcode(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Barcode cannot be null or blank");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Barcode barcode = (Barcode) o;
        return Objects.equals(value, barcode.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
