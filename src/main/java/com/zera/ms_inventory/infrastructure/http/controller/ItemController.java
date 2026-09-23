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
import com.zera.ms_inventory.core.usecase.item.ApproveItem;
import com.zera.ms_inventory.core.usecase.item.CreateItem;
import com.zera.ms_inventory.core.usecase.item.CreateItemResult;
import com.zera.ms_inventory.core.usecase.item.DeleteItem;
import com.zera.ms_inventory.core.usecase.item.FindItemByBarcode;
import com.zera.ms_inventory.core.usecase.item.FindItemById;
import com.zera.ms_inventory.core.usecase.item.ListItemEvents;
import com.zera.ms_inventory.core.usecase.item.ListItems;
import com.zera.ms_inventory.core.usecase.item.RejectItem;
import com.zera.ms_inventory.core.usecase.item.SubmitItem;
import com.zera.ms_inventory.core.usecase.item.UpdateItem;
import com.zera.ms_inventory.core.usecase.item.UploadItemPhoto;
import com.zera.ms_inventory.core.usecase.item.UpdateItemStatus;
import com.zera.ms_inventory.infrastructure.http.request.AssignItemUnitRequest;
import com.zera.ms_inventory.infrastructure.http.request.CreateItemRequest;
import com.zera.ms_inventory.infrastructure.http.request.RejectItemRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateItemRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateItemStatusRequest;
import com.zera.ms_inventory.infrastructure.http.response.EventResponse;
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
    private final ListItemEvents listItemEvents;
    private final FindItemById findItemById;
    private final FindItemByBarcode findItemByBarcode;
    private final UpdateItem updateItem;
    private final SubmitItem submitItem;
    private final ApproveItem approveItem;
    private final RejectItem rejectItem;
    private final UploadItemPhoto uploadItemPhoto;
    private final UpdateItemStatus updateItemStatus;
    private final AssignItemUnit assignItemUnit;
    private final DeleteItem deleteItem;

    public ItemController(CreateItem createItem,
                           ListItems listItems,
                           ListItemEvents listItemEvents,
                           FindItemById findItemById,
                           FindItemByBarcode findItemByBarcode,
                           UpdateItem updateItem,
                           SubmitItem submitItem,
                           ApproveItem approveItem,
                           RejectItem rejectItem,
                           UploadItemPhoto uploadItemPhoto,
                           UpdateItemStatus updateItemStatus,
                           AssignItemUnit assignItemUnit,
                           DeleteItem deleteItem,
                           ItemResponses itemResponses) {
        this.itemResponses = itemResponses;
        this.createItem = createItem;
        this.listItems = listItems;
        this.listItemEvents = listItemEvents;
        this.findItemById = findItemById;
        this.findItemByBarcode = findItemByBarcode;
        this.updateItem = updateItem;
        this.submitItem = submitItem;
        this.approveItem = approveItem;
        this.rejectItem = rejectItem;
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

    @GetMapping("/{id}/events")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<EventResponse>> findEvents(@RequestHeader("X-Unit-Id") UUID unitId,
                                                                  @PathVariable UUID id,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PageResponse.from(
                listItemEvents.execute(unitId, id, new Pagination(page, size)), EventResponse::from));
    }

    /** Envia o rascunho: 422 com missingFields quando ainda falta algo do cadastro. */
    @PostMapping("/{id}/submit")
    public ResponseEntity<ItemResponse> submit(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id,
                                               Actor actor) {
        return ResponseEntity.ok(itemResponses.from(submitItem.execute(unitId, id, actor)));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize(Authz.MANAGER)
    public ResponseEntity<ItemResponse> approve(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id,
                                                Actor actor) {
        return ResponseEntity.ok(itemResponses.from(approveItem.execute(unitId, id, actor)));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize(Authz.MANAGER)
    public ResponseEntity<ItemResponse> reject(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id,
                                               @RequestBody @Valid RejectItemRequest request, Actor actor) {
        return ResponseEntity.ok(itemResponses.from(rejectItem.execute(unitId, id, request.reason(), actor)));
    }

    // transicao fora da maquina de estados responde 409; os passos do fluxo ganham endpoint proprio nas ZERA-244 a 247
    @PatchMapping("/{id}/status")
    public ResponseEntity<ItemResponse> updateStatus(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id,
                                                     @RequestBody @Valid UpdateItemStatusRequest request, Actor actor) {
        return ResponseEntity.ok(itemResponses.from(updateItemStatus.execute(unitId, id, request.status(), actor)));
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
