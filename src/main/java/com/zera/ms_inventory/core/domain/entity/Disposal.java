package com.zera.ms_inventory.core.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.DestinationType;
import com.zera.ms_inventory.core.domain.valueobject.DisposedItem;

/**
 * Registro de descarte: o operador seleciona itens, escolhe o destino e diz onde e quando
 * entregou. Nao ha agendamento no sistema (decisao de produto da v1), entao o descarte e sempre
 * o registro de algo ja feito.
 */
public class Disposal {

    private final UUID id;
    private final UUID unitId;
    private DestinationType destination;
    /** Identificador do ponto na API externa de locais; nulo quando o destino nao tem ponto. */
    private final String placeId;
    private final String placeName;
    private final LocalDate disposedAt;
    private final String notes;
    private final List<DisposedItem> items;
    private final UUID createdBy;
    private final String createdByName;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Disposal(UUID id, UUID unitId, DestinationType destination, String placeId, String placeName,
                    LocalDate disposedAt, String notes, List<DisposedItem> items, UUID createdBy,
                    String createdByName, LocalDateTime createdAt, LocalDateTime updatedAt) {
        if (unitId == null) {
            throw new IllegalArgumentException("unitId is required");
        }
        if (destination == null) {
            throw new IllegalArgumentException("destination is required");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("a disposal needs at least one item");
        }
        if (disposedAt != null && disposedAt.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("disposedAt cannot be in the future");
        }
        this.id = id != null ? id : UUID.randomUUID();
        this.unitId = unitId;
        this.destination = destination;
        this.placeId = blankToNull(placeId);
        this.placeName = blankToNull(placeName);
        this.disposedAt = disposedAt != null ? disposedAt : LocalDate.now();
        this.notes = blankToNull(notes);
        this.items = List.copyOf(items);
        this.createdBy = createdBy;
        this.createdByName = createdByName;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    /** Descarte novo, registrado por quem esta com o token. */
    public static Disposal register(UUID unitId, DestinationType destination, String placeId, String placeName,
                                    LocalDate disposedAt, String notes, List<DisposedItem> items, Actor actor) {
        return new Disposal(null, unitId, destination, placeId, placeName, disposedAt, notes, items,
                actor != null ? actor.userId() : null, actor != null ? actor.name() : null, null, null);
    }

    /**
     * Correcao do destino informado por engano. Os itens ja sairam do estoque e continuam
     * descartados; o que muda e para onde foram, e com isso os indicadores de reciclagem.
     */
    public void correctDestination(DestinationType destination) {
        if (destination == null) {
            throw new IllegalArgumentException("destination is required");
        }
        this.destination = destination;
        this.updatedAt = LocalDateTime.now();
    }

    /** Peso total congelado; itens sem peso no modelo nao somam. */
    public double totalWeightKg() {
        return items.stream()
                .map(DisposedItem::weightKg)
                .filter(java.util.Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUnitId() {
        return unitId;
    }

    public DestinationType getDestination() {
        return destination;
    }

    public String getPlaceId() {
        return placeId;
    }

    public String getPlaceName() {
        return placeName;
    }

    public LocalDate getDisposedAt() {
        return disposedAt;
    }

    public String getNotes() {
        return notes;
    }

    public List<DisposedItem> getItems() {
        return items;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
