package com.zera.ms_inventory.infrastructure.http.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zera.ms_inventory.core.usecase.dashboard.GetDisposalIndicators;
import com.zera.ms_inventory.infrastructure.http.response.DisposalIndicatorsResponse;

@RestController
@RequestMapping("/api/v1/dashboard")
@PreAuthorize("isAuthenticated()")
public class DashboardController {

    private final GetDisposalIndicators getDisposalIndicators;

    public DashboardController(GetDisposalIndicators getDisposalIndicators) {
        this.getDisposalIndicators = getDisposalIndicators;
    }

    /** Sem datas, assume os ultimos 12 meses. */
    @GetMapping("/indicators")
    public ResponseEntity<DisposalIndicatorsResponse> indicators(
            @RequestHeader("X-Unit-Id") UUID unitId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(DisposalIndicatorsResponse.from(
                getDisposalIndicators.execute(unitId, from, to)));
    }
}
