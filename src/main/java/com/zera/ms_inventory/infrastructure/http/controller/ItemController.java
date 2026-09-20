package com.zera.ms_inventory.infrastructure.http.controller;

import java.io.IOException;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.ItemFilter;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
import com.zera.ms_inventory.core.usecase.item.AssignItemUnit;
import com.zera.ms_inventory.core.usecase.item.CreateItem;
import com.zera.ms_inventory.core.usecase.item.CreateItemResult;
import com.zera.ms_inventory.core.usecase.item.DeleteItem;
import com.zera.ms_inventory.core.usecase.item.FindItemByBarcode;
import com.zera.ms_inventory.core.usecase.item.FindItemById;
import com.zera.ms_inventory.core.usecase.item.ListItems;
import com.zera.ms_inventory.core.usecase.item.UpdateItem;
import com.zera.ms_inventory.core.usecase.item.UploadItemPhoto;
import com.zera.ms_inventory.core.usecase.item.UpdateItemStatus;
import com.zera.ms_inventory.infrastructure.http.request.AssignItemUnitRequest;
import com.zera.ms_inventory.infrastructure.http.request.CreateItemRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateItemRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateItemStatusRequest;
import com.zera.ms_inventory.infrastructure.http.response.ItemResponse;
import com.zera.ms_inventory.infrastructure.http.response.ItemResponses;
import com.zera.ms_inventory.infrastructure.http.response.PageResponse;
import com.zera.ms_inventory.infrastructure.security.Authz;

@RestController
@RequestMapping("/api/v1/items")
@PreAuthorize(Authz.INVENTORY_OPERATOR)
public class ItemController {

    private final ItemResponses itemResponses;
    private final CreateItem createItem;
    private final ListItems listItems;
    private final FindItemById findItemById;
    private final FindItemByBarcode findItemByBarcode;
    private final UpdateItem updateItem;
    private final UploadItemPhoto uploadItemPhoto;
    private final UpdateItemStatus updateItemStatus;
    private final AssignItemUnit assignItemUnit;
    private final DeleteItem deleteItem;

    public ItemController(CreateItem createItem,
                           ListItems listItems,
                           FindItemById findItemById,
                           FindItemByBarcode findItemByBarcode,
                           UpdateItem updateItem,
                           UploadItemPhoto uploadItemPhoto,
                           UpdateItemStatus updateItemStatus,
                           AssignItemUnit assignItemUnit,
                           DeleteItem deleteItem,
                           ItemResponses itemResponses) {
        this.itemResponses = itemResponses;
        this.createItem = createItem;
        this.listItems = listItems;
        this.findItemById = findItemById;
        this.findItemByBarcode = findItemByBarcode;
        this.updateItem = updateItem;
        this.uploadItemPhoto = uploadItemPhoto;
        this.updateItemStatus = updateItemStatus;
        this.assignItemUnit = assignItemUnit;
        this.deleteItem = deleteItem;
    }

    @PostMapping
    public ResponseEntity<ItemResponse> create(@RequestHeader("X-Unit-Id") UUID unitId,
                                        @RequestBody @Valid CreateItemRequest request, Actor actor) {
        CreateItemResult result = createItem.execute(request.toCommand(unitId, actor));
        // reenvio do mesmo id (app offline) devolve o item ja cadastrado
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(itemResponses.from(result.item()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<ItemResponse>> findAll(@RequestHeader("X-Unit-Id") UUID unitId,
                                                            @RequestParam(required = false) ItemStatus status,
                                                            @RequestParam(required = false) UUID categoryId,
                                                            @RequestParam(required = false) UUID modelId,
                                                            @RequestParam(required = false) String q,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "20") int size) {
        ItemFilter filter = new ItemFilter(status, categoryId, modelId, q);
        return ResponseEntity.ok(PageResponse.from(listItems.execute(unitId, filter, new Pagination(page, size)),
                itemResponses::from));
    }

    @GetMapping("/by-barcode/{barcode}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ItemResponse> findByBarcode(@RequestHeader("X-Unit-Id") UUID unitId,
                                                      @PathVariable String barcode) {
        return ResponseEntity.ok(itemResponses.from(findItemByBarcode.execute(unitId, barcode)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ItemResponse> findById(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id) {
        return ResponseEntity.ok(itemResponses.from(findItemById.execute(unitId, id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ItemResponse> update(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id,
                                               @RequestBody @Valid UpdateItemRequest request) {
        return ResponseEntity.ok(itemResponses.from(updateItem.execute(request.toCommand(unitId, id))));
    }

    @PostMapping(path = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ItemResponse> uploadPhoto(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id,
                                                    @RequestPart("photo") MultipartFile photo) throws IOException {
        return ResponseEntity.ok(itemResponses.from(
                uploadItemPhoto.execute(unitId, id, photo.getBytes(), photo.getContentType())));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ItemResponse> updateStatus(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id, @RequestBody @Valid UpdateItemStatusRequest request) {
        return ResponseEntity.ok(itemResponses.from(updateItemStatus.execute(unitId, id, request.status())));
    }

    @PatchMapping("/{id}/unit")
    @PreAuthorize(Authz.MANAGER)
    public ResponseEntity<ItemResponse> assignUnit(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id, @RequestBody @Valid AssignItemUnitRequest request) {
        return ResponseEntity.ok(itemResponses.from(assignItemUnit.execute(unitId, id, request.unitId())));
    }

    // exclusao ainda e fisica: so o gestor ate a remocao logica revisavel (ZERA-247)
    @DeleteMapping("/{id}")
    @PreAuthorize(Authz.MANAGER)
    public ResponseEntity<Void> delete(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id) {
        deleteItem.execute(unitId, id);
        return ResponseEntity.noContent().build();
    }
}
