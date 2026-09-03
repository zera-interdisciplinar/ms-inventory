package com.zera.ms_inventory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Category;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;

/** Objetos de dominio prontos. UNIT e OTHER_UNIT existem para provar o isolamento entre unidades. */
public final class Fixtures {

    public static final UUID UNIT = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    public static final UUID OTHER_UNIT = UUID.fromString("00000000-0000-0000-0000-0000000000b2");

    private Fixtures() {
    }

    public static Category category(UUID unitId) {
        return category(UUID.randomUUID(), unitId);
    }

    public static Category category(UUID id, UUID unitId) {
        return new Category(id, unitId, "Electronics", "Devices and components");
    }

    public static Model model(UUID unitId) {
        return model(UUID.randomUUID(), unitId);
    }

    public static Model model(UUID id, UUID unitId) {
        return new Model(id, unitId, "Laptop X1", "Acme", 24, 60, Set.of("Lithium"), category(unitId));
    }

    public static Item item(UUID unitId) {
        return item(UUID.randomUUID(), unitId);
    }

    public static Item item(UUID id, UUID unitId) {
        return item(id, unitId, model(unitId));
    }

    public static Item item(UUID id, UUID unitId, Model model) {
        return new Item(id, new Barcode("7891234567890"), ItemStatus.OK, unitId, model,
                LocalDateTime.of(2026, 8, 10, 8, 0), 2024, 7, "SN-001", LocalDate.of(2026, 8, 4));
    }
}
