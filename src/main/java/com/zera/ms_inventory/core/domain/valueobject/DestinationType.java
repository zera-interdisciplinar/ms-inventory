package com.zera.ms_inventory.core.domain.valueobject;

/** Destino do descarte. O ponto fisico vem de uma API externa de locais, nao do nosso catalogo. */
public enum DestinationType {
    RECYCLING,
    LANDFILL,
    DONATION
}
