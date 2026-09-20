package com.zera.ms_inventory.infrastructure.http.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
import com.zera.ms_inventory.core.usecase.model.CreateModel;
import com.zera.ms_inventory.core.usecase.model.DeleteModel;
import com.zera.ms_inventory.core.usecase.model.FindModelById;
import com.zera.ms_inventory.core.usecase.model.ListModels;
import com.zera.ms_inventory.core.usecase.model.UpdateModelExpectedLifespanMonths;
import com.zera.ms_inventory.core.usecase.model.UpdateModelManufacturer;
import com.zera.ms_inventory.core.usecase.model.UpdateModelMaterials;
import com.zera.ms_inventory.core.usecase.model.UpdateModelName;
import com.zera.ms_inventory.core.usecase.model.UpdateModelWarrantyMonths;
import com.zera.ms_inventory.infrastructure.http.request.CreateModelRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateModelExpectedLifespanMonthsRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateModelManufacturerRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateModelMaterialsRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateModelNameRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateModelWarrantyMonthsRequest;
import com.zera.ms_inventory.infrastructure.http.response.ModelResponse;
import com.zera.ms_inventory.infrastructure.http.response.PageResponse;
import com.zera.ms_inventory.infrastructure.security.Authz;

@RestController
@RequestMapping("/api/v1/models")
@PreAuthorize(Authz.INVENTORY_OPERATOR)
public class ModelController {

    private final CreateModel createModel;
    private final ListModels listModels;
    private final FindModelById findModelById;
    private final UpdateModelName updateModelName;
    private final UpdateModelManufacturer updateModelManufacturer;
    private final UpdateModelWarrantyMonths updateModelWarrantyMonths;
    private final UpdateModelExpectedLifespanMonths updateModelExpectedLifespanMonths;
    private final UpdateModelMaterials updateModelMaterials;
    private final DeleteModel deleteModel;

    public ModelController(CreateModel createModel,
                            ListModels listModels,
                            FindModelById findModelById,
                            UpdateModelName updateModelName,
                            UpdateModelManufacturer updateModelManufacturer,
                            UpdateModelWarrantyMonths updateModelWarrantyMonths,
                            UpdateModelExpectedLifespanMonths updateModelExpectedLifespanMonths,
                            UpdateModelMaterials updateModelMaterials,
                            DeleteModel deleteModel) {
        this.createModel = createModel;
        this.listModels = listModels;
        this.findModelById = findModelById;
        this.updateModelName = updateModelName;
        this.updateModelManufacturer = updateModelManufacturer;
        this.updateModelWarrantyMonths = updateModelWarrantyMonths;
        this.updateModelExpectedLifespanMonths = updateModelExpectedLifespanMonths;
        this.updateModelMaterials = updateModelMaterials;
        this.deleteModel = deleteModel;
    }

    @PostMapping
    public ResponseEntity<ModelResponse> create(@RequestHeader("X-Unit-Id") UUID unitId,
                                         @RequestBody @Valid CreateModelRequest request, Actor actor) {
        Model created = createModel.execute(request.toCommand(unitId, actor));
        return ResponseEntity.status(HttpStatus.CREATED).body(ModelResponse.from(created));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<ModelResponse>> findAll(@RequestHeader("X-Unit-Id") UUID unitId,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PageResponse.from(listModels.execute(unitId, new Pagination(page, size)), ModelResponse::from));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ModelResponse> findById(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id) {
        return ResponseEntity.ok(ModelResponse.from(findModelById.execute(unitId, id)));
    }

    @PatchMapping("/{id}/name")
    public ResponseEntity<ModelResponse> rename(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id, @RequestBody @Valid UpdateModelNameRequest request) {
        return ResponseEntity.ok(ModelResponse.from(updateModelName.execute(unitId, id, request.name())));
    }

    @PatchMapping("/{id}/manufacturer")
    public ResponseEntity<ModelResponse> updateManufacturer(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id, @RequestBody @Valid UpdateModelManufacturerRequest request) {
        return ResponseEntity.ok(ModelResponse.from(updateModelManufacturer.execute(unitId, id, request.manufacturer())));
    }

    @PatchMapping("/{id}/warranty-months")
    public ResponseEntity<ModelResponse> updateWarrantyMonths(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id, @RequestBody @Valid UpdateModelWarrantyMonthsRequest request) {
        return ResponseEntity.ok(ModelResponse.from(updateModelWarrantyMonths.execute(unitId, id, request.warrantyMonths())));
    }

    @PatchMapping("/{id}/expected-lifespan-months")
    public ResponseEntity<ModelResponse> updateExpectedLifespanMonths(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id, @RequestBody @Valid UpdateModelExpectedLifespanMonthsRequest request) {
        return ResponseEntity.ok(ModelResponse.from(updateModelExpectedLifespanMonths.execute(unitId, id, request.expectedLifespanMonths())));
    }

    @PatchMapping("/{id}/materials")
    public ResponseEntity<ModelResponse> updateMaterials(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id, @RequestBody @Valid UpdateModelMaterialsRequest request) {
        return ResponseEntity.ok(ModelResponse.from(updateModelMaterials.execute(unitId, id, request.materials())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(Authz.MANAGER)
    public ResponseEntity<Void> delete(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id) {
        deleteModel.execute(unitId, id);
        return ResponseEntity.noContent().build();
    }
}
