package com.zera.ms_inventory.infrastructure.http.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.usecase.item.AssignItemUnit;
import com.zera.ms_inventory.core.usecase.item.CreateItem;
import com.zera.ms_inventory.core.usecase.item.DeleteItem;
import com.zera.ms_inventory.core.usecase.item.FindAllItems;
import com.zera.ms_inventory.core.usecase.item.FindItemById;
import com.zera.ms_inventory.core.usecase.item.UpdateItemAcquiredAt;
import com.zera.ms_inventory.core.usecase.item.UpdateItemManufacturingDate;
import com.zera.ms_inventory.core.usecase.item.UpdateItemNextPredictionDate;
import com.zera.ms_inventory.core.usecase.item.UpdateItemSerialNumber;
import com.zera.ms_inventory.core.usecase.item.UpdateItemStatus;
import com.zera.ms_inventory.core.usecase.item.UpdateItemUsageIntensity;
import com.zera.ms_inventory.infrastructure.http.request.AssignItemUnitRequest;
import com.zera.ms_inventory.infrastructure.http.request.CreateItemRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateItemAcquiredAtRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateItemManufacturingDateRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateItemNextPredictionDateRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateItemSerialNumberRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateItemStatusRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateItemUsageIntensityRequest;

@RestController
@RequestMapping("/api/v1/items")
public class ItemController {

    private final CreateItem createItem;
    private final FindAllItems findAllItems;
    private final FindItemById findItemById;
    private final UpdateItemStatus updateItemStatus;
    private final AssignItemUnit assignItemUnit;
    private final UpdateItemSerialNumber updateItemSerialNumber;
    private final UpdateItemAcquiredAt updateItemAcquiredAt;
    private final UpdateItemNextPredictionDate updateItemNextPredictionDate;
    private final UpdateItemManufacturingDate updateItemManufacturingDate;
    private final UpdateItemUsageIntensity updateItemUsageIntensity;
    private final DeleteItem deleteItem;

    public ItemController(CreateItem createItem,
                           FindAllItems findAllItems,
                           FindItemById findItemById,
                           UpdateItemStatus updateItemStatus,
                           AssignItemUnit assignItemUnit,
                           UpdateItemSerialNumber updateItemSerialNumber,
                           UpdateItemAcquiredAt updateItemAcquiredAt,
                           UpdateItemNextPredictionDate updateItemNextPredictionDate,
                           UpdateItemManufacturingDate updateItemManufacturingDate,
                           UpdateItemUsageIntensity updateItemUsageIntensity,
                           DeleteItem deleteItem) {
        this.createItem = createItem;
        this.findAllItems = findAllItems;
        this.findItemById = findItemById;
        this.updateItemStatus = updateItemStatus;
        this.assignItemUnit = assignItemUnit;
        this.updateItemSerialNumber = updateItemSerialNumber;
        this.updateItemAcquiredAt = updateItemAcquiredAt;
        this.updateItemNextPredictionDate = updateItemNextPredictionDate;
        this.updateItemManufacturingDate = updateItemManufacturingDate;
        this.updateItemUsageIntensity = updateItemUsageIntensity;
        this.deleteItem = deleteItem;
    }

    @PostMapping
    public ResponseEntity<Item> create(@RequestHeader("X-Unit-Id") UUID unitId,
                                        @RequestBody @Valid CreateItemRequest request) {
        Item created = createItem.execute(request.toCommand(unitId));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Item>> findAll(@RequestHeader("X-Unit-Id") UUID unitId) {
        return ResponseEntity.ok(findAllItems.execute(unitId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> findById(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id) {
        return ResponseEntity.ok(findItemById.execute(unitId, id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Item> updateStatus(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id, @RequestBody @Valid UpdateItemStatusRequest request) {
        return ResponseEntity.ok(updateItemStatus.execute(unitId, id, request.status()));
    }

    @PatchMapping("/{id}/unit")
    public ResponseEntity<Item> assignUnit(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id, @RequestBody @Valid AssignItemUnitRequest request) {
        return ResponseEntity.ok(assignItemUnit.execute(unitId, id, request.unitId()));
    }

    @PatchMapping("/{id}/serial-number")
    public ResponseEntity<Item> updateSerialNumber(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id, @RequestBody @Valid UpdateItemSerialNumberRequest request) {
        return ResponseEntity.ok(updateItemSerialNumber.execute(unitId, id, request.serialNumber()));
    }

    @PatchMapping("/{id}/acquired-at")
    public ResponseEntity<Item> updateAcquiredAt(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id, @RequestBody @Valid UpdateItemAcquiredAtRequest request) {
        return ResponseEntity.ok(updateItemAcquiredAt.execute(unitId, id, request.acquiredAt()));
    }

    @PatchMapping("/{id}/next-prediction-date")
    public ResponseEntity<Item> updateNextPredictionDate(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id, @RequestBody @Valid UpdateItemNextPredictionDateRequest request) {
        return ResponseEntity.ok(updateItemNextPredictionDate.execute(unitId, id, request.nextPredictionDate()));
    }

    @PatchMapping("/{id}/manufacturing-date")
    public ResponseEntity<Item> updateManufacturingDate(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id, @RequestBody @Valid UpdateItemManufacturingDateRequest request) {
        return ResponseEntity.ok(updateItemManufacturingDate.execute(unitId, id, request.manufacturingDate()));
    }

    @PatchMapping("/{id}/usage-intensity")
    public ResponseEntity<Item> updateUsageIntensity(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id, @RequestBody @Valid UpdateItemUsageIntensityRequest request) {
        return ResponseEntity.ok(updateItemUsageIntensity.execute(unitId, id, request.usageIntensity()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id) {
        deleteItem.execute(unitId, id);
        return ResponseEntity.noContent().build();
    }
}
