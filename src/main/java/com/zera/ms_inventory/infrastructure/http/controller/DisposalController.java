package com.zera.ms_inventory.infrastructure.http.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
import com.zera.ms_inventory.core.usecase.disposal.CorrectDisposalDestination;
import com.zera.ms_inventory.core.usecase.disposal.CreateDisposal;
import com.zera.ms_inventory.core.usecase.disposal.FindDisposalById;
import com.zera.ms_inventory.core.usecase.disposal.ListDisposals;
import com.zera.ms_inventory.infrastructure.http.request.CorrectDisposalDestinationRequest;
import com.zera.ms_inventory.infrastructure.http.request.CreateDisposalRequest;
import com.zera.ms_inventory.infrastructure.http.response.DisposalResponse;
import com.zera.ms_inventory.infrastructure.http.response.PageResponse;
import com.zera.ms_inventory.infrastructure.security.Authz;

/** Operario e gestor registram descarte (decisao de produto da v1). */
@RestController
@RequestMapping("/api/v1/disposals")
@PreAuthorize(Authz.INVENTORY_OPERATOR)
public class DisposalController {

    private final CreateDisposal createDisposal;
    private final ListDisposals listDisposals;
    private final FindDisposalById findDisposalById;
    private final CorrectDisposalDestination correctDisposalDestination;

    public DisposalController(CreateDisposal createDisposal,
                              ListDisposals listDisposals,
                              FindDisposalById findDisposalById,
                              CorrectDisposalDestination correctDisposalDestination) {
        this.createDisposal = createDisposal;
        this.listDisposals = listDisposals;
        this.findDisposalById = findDisposalById;
        this.correctDisposalDestination = correctDisposalDestination;
    }

    @PostMapping
    public ResponseEntity<DisposalResponse> create(@RequestHeader("X-Unit-Id") UUID unitId,
                                                   @RequestBody @Valid CreateDisposalRequest request,
                                                   Actor actor) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DisposalResponse.from(createDisposal.execute(request.toCommand(unitId, actor))));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<DisposalResponse>> findAll(@RequestHeader("X-Unit-Id") UUID unitId,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PageResponse.from(
                listDisposals.execute(unitId, new Pagination(page, size)), DisposalResponse::from));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DisposalResponse> findById(@RequestHeader("X-Unit-Id") UUID unitId,
                                                     @PathVariable UUID id) {
        return ResponseEntity.ok(DisposalResponse.from(findDisposalById.execute(unitId, id)));
    }

    /** Corrige o destino informado por engano; os itens continuam descartados. */
    @PatchMapping("/{id}")
    public ResponseEntity<DisposalResponse> correctDestination(
            @RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id,
            @RequestBody @Valid CorrectDisposalDestinationRequest request) {
        return ResponseEntity.ok(DisposalResponse.from(
                correctDisposalDestination.execute(unitId, id, request.destination())));
    }
}
