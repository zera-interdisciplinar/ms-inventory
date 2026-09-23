package com.zera.ms_inventory.core.domain.valueobject;

/** Codigos do catalogo global de materiais (seed em neo4j/migrations/V002__material_catalog.cypher). */
public enum MaterialCode {
    PLASTIC,
    METAL,
    GLASS,
    PAPER,
    BATTERY,
    CIRCUIT_BOARD,
    CABLE,
    SCREEN,
    OTHER
}
