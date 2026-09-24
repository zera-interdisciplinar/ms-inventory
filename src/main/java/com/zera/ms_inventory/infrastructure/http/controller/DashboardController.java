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

import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
import com.zera.ms_inventory.core.usecase.dashboard.GetDisposalIndicators;
import com.zera.ms_inventory.core.usecase.dashboard.GetHomeSummary;
import com.zera.ms_inventory.core.usecase.dashboard.GetWorkCenter;
import com.zera.ms_inventory.infrastructure.http.response.DisposalIndicatorsResponse;
import com.zera.ms_inventory.infrastructure.http.response.HomeSummaryResponse;
import com.zera.ms_inventory.infrastructure.http.response.ItemResponses;
import com.zera.ms_inventory.infrastructure.http.response.WorkCenterResponse;

@RestController
@RequestMapping("/api/v1/dashboard")
@PreAuthorize("isAuthenticated()")
public class DashboardController {

    private final GetDisposalIndicators getDisposalIndicators;
    private final GetHomeSummary getHomeSummary;
    private final GetWorkCenter getWorkCenter;
    private final ItemResponses itemResponses;

    public DashboardController(GetDisposalIndicators getDisposalIndicators,
                               GetHomeSummary getHomeSummary,
                               GetWorkCenter getWorkCenter,
                               ItemResponses itemResponses) {
        this.getDisposalIndicators = getDisposalIndicators;
        this.getHomeSummary = getHomeSummary;
        this.getWorkCenter = getWorkCenter;
        this.itemResponses = itemResponses;
    }

    /**
     * Painel inicial da unidade: estoque, ocupacao, pendencias e ultimos itens. A paginacao vale
     * so para os itens recentes; os contadores sao sempre da unidade inteira.
     */
    @GetMapping("/home")
    public ResponseEntity<HomeSummaryResponse> home(@RequestHeader("X-Unit-Id") UUID unitId,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(HomeSummaryResponse.from(
                getHomeSummary.execute(unitId, new Pagination(page, size)), itemResponses));
    }

    /** Central de Trabalho: o que quem esta logado precisa resolver. */
    @GetMapping("/work-center")
    public ResponseEntity<WorkCenterResponse> workCenter(@RequestHeader("X-Unit-Id") UUID unitId, Actor actor) {
        return ResponseEntity.ok(WorkCenterResponse.from(getWorkCenter.execute(unitId, actor)));
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
