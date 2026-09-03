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

import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.usecase.model.CreateModel;
import com.zera.ms_inventory.core.usecase.model.DeleteModel;
import com.zera.ms_inventory.core.usecase.model.FindAllModels;
import com.zera.ms_inventory.core.usecase.model.FindModelById;
import com.zera.ms_inventory.core.usecase.model.UpdateModelExpectedLifespanMonths;
import com.zera.ms_inventory.core.usecase.model.UpdateModelHazardousMaterials;
import com.zera.ms_inventory.core.usecase.model.UpdateModelManufacturer;
import com.zera.ms_inventory.core.usecase.model.UpdateModelName;
import com.zera.ms_inventory.core.usecase.model.UpdateModelWarrantyMonths;
import com.zera.ms_inventory.infrastructure.http.request.CreateModelRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateModelExpectedLifespanMonthsRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateModelHazardousMaterialsRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateModelManufacturerRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateModelNameRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateModelWarrantyMonthsRequest;

@RestController
@RequestMapping("/api/v1/models")
public class ModelController {

    private final CreateModel createModel;
    private final FindAllModels findAllModels;
    private final FindModelById findModelById;
    private final UpdateModelName updateModelName;
    private final UpdateModelManufacturer updateModelManufacturer;
    private final UpdateModelWarrantyMonths updateModelWarrantyMonths;
    private final UpdateModelExpectedLifespanMonths updateModelExpectedLifespanMonths;
    private final UpdateModelHazardousMaterials updateModelHazardousMaterials;
    private final DeleteModel deleteModel;

    public ModelController(CreateModel createModel,
                            FindAllModels findAllModels,
                            FindModelById findModelById,
                            UpdateModelName updateModelName,
                            UpdateModelManufacturer updateModelManufacturer,
                            UpdateModelWarrantyMonths updateModelWarrantyMonths,
                            UpdateModelExpectedLifespanMonths updateModelExpectedLifespanMonths,
                            UpdateModelHazardousMaterials updateModelHazardousMaterials,
                            DeleteModel deleteModel) {
        this.createModel = createModel;
        this.findAllModels = findAllModels;
        this.findModelById = findModelById;
        this.updateModelName = updateModelName;
        this.updateModelManufacturer = updateModelManufacturer;
        this.updateModelWarrantyMonths = updateModelWarrantyMonths;
        this.updateModelExpectedLifespanMonths = updateModelExpectedLifespanMonths;
        this.updateModelHazardousMaterials = updateModelHazardousMaterials;
        this.deleteModel = deleteModel;
    }

    @PostMapping
    public ResponseEntity<Model> create(@RequestHeader("X-Unit-Id") UUID unitId,
                                         @RequestBody @Valid CreateModelRequest request) {
        Model created = createModel.execute(unitId, request.name(), request.manufacturer(), request.warrantyMonths(),
                request.expectedLifespanMonths(), request.hazardousMaterials(), request.categoryId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Model>> findAll(@RequestHeader("X-Unit-Id") UUID unitId) {
        return ResponseEntity.ok(findAllModels.execute(unitId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Model> findById(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id) {
        return ResponseEntity.ok(findModelById.execute(unitId, id));
    }

    @PatchMapping("/{id}/name")
    public ResponseEntity<Model> rename(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id, @RequestBody @Valid UpdateModelNameRequest request) {
        return ResponseEntity.ok(updateModelName.execute(unitId, id, request.name()));
    }

    @PatchMapping("/{id}/manufacturer")
    public ResponseEntity<Model> updateManufacturer(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id, @RequestBody @Valid UpdateModelManufacturerRequest request) {
        return ResponseEntity.ok(updateModelManufacturer.execute(unitId, id, request.manufacturer()));
    }

    @PatchMapping("/{id}/warranty-months")
    public ResponseEntity<Model> updateWarrantyMonths(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id, @RequestBody @Valid UpdateModelWarrantyMonthsRequest request) {
        return ResponseEntity.ok(updateModelWarrantyMonths.execute(unitId, id, request.warrantyMonths()));
    }

    @PatchMapping("/{id}/expected-lifespan-months")
    public ResponseEntity<Model> updateExpectedLifespanMonths(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id, @RequestBody @Valid UpdateModelExpectedLifespanMonthsRequest request) {
        return ResponseEntity.ok(updateModelExpectedLifespanMonths.execute(unitId, id, request.expectedLifespanMonths()));
    }

    @PatchMapping("/{id}/hazardous-materials")
    public ResponseEntity<Model> updateHazardousMaterials(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id, @RequestBody @Valid UpdateModelHazardousMaterialsRequest request) {
        return ResponseEntity.ok(updateModelHazardousMaterials.execute(unitId, id, request.hazardousMaterials()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id) {
        deleteModel.execute(unitId, id);
        return ResponseEntity.noContent().build();
    }
}
