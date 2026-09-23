package com.zera.ms_inventory.core.usecase.disposal;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.DestinationType;

public record CreateDisposalCommand(
        UUID unitId,
        DestinationType destination,
        String placeId,
        String placeName,
        LocalDate disposedAt,
        String notes,
        List<UUID> itemIds,
        Actor actor
) {}
