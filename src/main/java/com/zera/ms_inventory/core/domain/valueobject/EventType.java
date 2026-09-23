package com.zera.ms_inventory.core.domain.valueobject;

/**
 * Tipo do evento gravado a cada passo do ciclo de vida do item. Vira label do no no Neo4j
 * ({@code :Event:APPROVED}), entao o historico pode ser filtrado por tipo direto no grafo.
 */
public enum EventType {
    CREATED,
    SUBMITTED,
    APPROVED,
    REJECTED,
    MAINTENANCE_STARTED,
    MAINTENANCE_FINISHED,
    EVALUATED,
    DISPOSED,
    REMOVED,
    RESTORED,
    /** Mudanca de status pelo endpoint generico, sem um passo de fluxo dedicado. */
    STATUS_CHANGED
}
