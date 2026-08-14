package com.zera.ms_inventory.infrastructure.http.request;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.usecase.item.CreateItemCommand;

public record CreateItemRequest(
        @NotBlank String barcode,
        @NotNull ItemStatus status,
        @NotNull UUID unitId,
        LocalDateTime nextPredictionDate,
        Integer manufacturingDate,
        Integer usageIntensity,
        String serialNumber,
        LocalDate acquiredAt
) {
    public CreateItemCommand toCommand() {
        return new CreateItemCommand(new Barcode(barcode), status, unitId, nextPredictionDate, manufacturingDate,
                usageIntensity, serialNumber, acquiredAt);
    }
}
