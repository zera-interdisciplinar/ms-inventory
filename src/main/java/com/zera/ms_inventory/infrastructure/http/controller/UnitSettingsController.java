package com.zera.ms_inventory.infrastructure.http.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.usecase.unit.GetUnitSettings;
import com.zera.ms_inventory.core.usecase.unit.UpdateUnitSettings;
import com.zera.ms_inventory.infrastructure.http.request.UpdateUnitSettingsRequest;
import com.zera.ms_inventory.infrastructure.http.response.UnitSettingsResponse;
import com.zera.ms_inventory.infrastructure.security.Authz;

@RestController
@RequestMapping("/api/v1/unit-settings")
public class UnitSettingsController {

    private final GetUnitSettings getUnitSettings;
    private final UpdateUnitSettings updateUnitSettings;

    public UnitSettingsController(GetUnitSettings getUnitSettings, UpdateUnitSettings updateUnitSettings) {
        this.getUnitSettings = getUnitSettings;
        this.updateUnitSettings = updateUnitSettings;
    }

    /** Qualquer autenticado le: o operario ve a ocupacao no painel. */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UnitSettingsResponse> find(@RequestHeader("X-Unit-Id") UUID unitId) {
        return ResponseEntity.ok(UnitSettingsResponse.from(getUnitSettings.execute(unitId)));
    }

    @PutMapping
    @PreAuthorize(Authz.MANAGER)
    public ResponseEntity<UnitSettingsResponse> update(@RequestHeader("X-Unit-Id") UUID unitId,
                                                       @RequestBody @Valid UpdateUnitSettingsRequest request,
                                                       Actor actor) {
        return ResponseEntity.ok(UnitSettingsResponse.from(
                updateUnitSettings.execute(unitId, request.stockCapacity(), actor)));
    }
}
