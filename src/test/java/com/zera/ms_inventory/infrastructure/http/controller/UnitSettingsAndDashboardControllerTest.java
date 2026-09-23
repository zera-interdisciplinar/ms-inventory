package com.zera.ms_inventory.infrastructure.http.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.UnitInventorySettings;
import com.zera.ms_inventory.core.domain.valueobject.DisposalIndicators;
import com.zera.ms_inventory.core.domain.valueobject.MaterialCode;
import com.zera.ms_inventory.core.usecase.dashboard.GetDisposalIndicators;
import com.zera.ms_inventory.core.usecase.unit.GetUnitSettings;
import com.zera.ms_inventory.core.usecase.unit.UpdateUnitSettings;
import com.zera.ms_inventory.infrastructure.http.handler.GlobalExceptionHandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {UnitSettingsController.class, DashboardController.class})
@org.springframework.context.annotation.Import({GlobalExceptionHandler.class,
        com.zera.ms_inventory.infrastructure.http.response.ItemResponses.class})
class UnitSettingsAndDashboardControllerTest {

    private static final UUID UNIT = Fixtures.UNIT;
    private static final UUID MANAGER_ID = UUID.fromString("00000000-0000-0000-0000-0000000000c3");

    @Autowired private MockMvc mockMvc;

    @MockitoBean private GetUnitSettings getUnitSettings;
    @MockitoBean private UpdateUnitSettings updateUnitSettings;
    @MockitoBean private GetDisposalIndicators getDisposalIndicators;
    @MockitoBean private com.zera.ms_inventory.core.usecase.dashboard.GetHomeSummary getHomeSummary;
    @MockitoBean private com.zera.ms_inventory.core.usecase.dashboard.GetWorkCenter getWorkCenter;
    @MockitoBean private com.zera.ms_inventory.core.repository.PhotoStorage photoStorage;

    @Test
    @DisplayName("GET /api/v1/unit-settings - unidade sem configuracao responde configured=false")
    void shouldReturnUnconfiguredSettings() throws Exception {
        when(getUnitSettings.execute(UNIT)).thenReturn(UnitInventorySettings.notConfigured(UNIT));

        mockMvc.perform(get("/api/v1/unit-settings").header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configured").value(false))
                .andExpect(jsonPath("$.stockCapacity").doesNotExist());
    }

    @Test
    @DisplayName("PUT /api/v1/unit-settings - gestor define a capacidade")
    void shouldUpdateCapacity() throws Exception {
        UnitInventorySettings settings = UnitInventorySettings.notConfigured(UNIT);
        settings.changeCapacity(500, Fixtures.MANAGER);
        when(updateUnitSettings.execute(eq(UNIT), eq(500), any())).thenReturn(settings);

        mockMvc.perform(put("/api/v1/unit-settings")
                        .principal(new TestingAuthenticationToken(MANAGER_ID.toString(), null, "ROLE_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stockCapacity\":500}")
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockCapacity").value(500))
                .andExpect(jsonPath("$.configured").value(true))
                .andExpect(jsonPath("$.updatedByName").value("Kevin Gestor"));
    }

    @Test
    @DisplayName("PUT /api/v1/unit-settings - capacidade negativa e 400")
    void shouldRejectNegativeCapacity() throws Exception {
        mockMvc.perform(put("/api/v1/unit-settings")
                        .principal(new TestingAuthenticationToken(MANAGER_ID.toString(), null, "ROLE_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stockCapacity\":-1}")
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/dashboard/home - devolve estoque, ocupacao e pendencias")
    void shouldReturnHomePanel() throws Exception {
        when(getHomeSummary.execute(UNIT)).thenReturn(
                new com.zera.ms_inventory.core.domain.valueobject.HomeSummary(
                        120L, 20.0, 200, 60.0, 3L, 2L, 1L, 7L, 30,
                        List.of(Fixtures.item(UNIT))));

        mockMvc.perform(get("/api/v1/dashboard/home").header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeItems").value(120))
                .andExpect(jsonPath("$.activeItemsChangePercent").value(20.0))
                .andExpect(jsonPath("$.occupancyPercent").value(60.0))
                .andExpect(jsonPath("$.pendingApproval").value(3))
                .andExpect(jsonPath("$.disposalsInWindow").value(7))
                .andExpect(jsonPath("$.windowDays").value(30))
                .andExpect(jsonPath("$.recentItems[0].barcode").value("7891234567890"));
    }

    /** Unidade sem capacidade: a ocupacao nao vem, em vez de vir zerada. */
    @Test
    @DisplayName("GET /api/v1/dashboard/home - ocupacao ausente sem capacidade")
    void shouldOmitOccupancyWithoutCapacity() throws Exception {
        when(getHomeSummary.execute(UNIT)).thenReturn(
                new com.zera.ms_inventory.core.domain.valueobject.HomeSummary(
                        5L, null, null, null, 0L, 0L, 0L, 0L, 30, List.of()));

        mockMvc.perform(get("/api/v1/dashboard/home").header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.occupancyPercent").doesNotExist())
                .andExpect(jsonPath("$.activeItemsChangePercent").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/v1/dashboard/work-center - traz o que falta e o motivo da reprovacao")
    void shouldReturnWorkCenter() throws Exception {
        com.zera.ms_inventory.core.domain.entity.Item rascunho = Fixtures.item(UNIT);
        com.zera.ms_inventory.core.domain.entity.Item reprovado = Fixtures.item(UNIT);
        when(getWorkCenter.execute(eq(UNIT), any())).thenReturn(
                new com.zera.ms_inventory.core.domain.valueobject.WorkCenterSummary(
                        List.of(rascunho), List.of(reprovado),
                        java.util.Map.of(reprovado.getId(), "Foto ilegivel"),
                        List.of(), 2L, 1L));

        mockMvc.perform(get("/api/v1/dashboard/work-center")
                        .principal(new TestingAuthenticationToken(MANAGER_ID.toString(), null, "ROLE_EMPLOYEE"))
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.drafts[0].missingFields").isArray())
                .andExpect(jsonPath("$.rejected[0].rejectionReason").value("Foto ilegivel"))
                .andExpect(jsonPath("$.inMaintenance").value(2))
                .andExpect(jsonPath("$.awaitingEvaluation").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/dashboard/indicators - devolve kg por mes, materiais e taxa")
    void shouldReturnIndicators() throws Exception {
        when(getDisposalIndicators.execute(eq(UNIT), any(), any())).thenReturn(new DisposalIndicators(
                LocalDate.parse("2026-01-01"), LocalDate.parse("2026-02-28"), 10.0, 60.0, 25.0, 100.0,
                List.of(new DisposalIndicators.MonthlyWeight("2026-01", 4.0),
                        new DisposalIndicators.MonthlyWeight("2026-02", 6.0)),
                List.of(new DisposalIndicators.MaterialShare(MaterialCode.METAL, 4.0, 40.0))));

        mockMvc.perform(get("/api/v1/dashboard/indicators")
                        .param("from", "2026-01-01")
                        .param("to", "2026-02-28")
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalWeightKg").value(10.0))
                .andExpect(jsonPath("$.recyclingRatePercent").value(60.0))
                .andExpect(jsonPath("$.recyclingRateChangePoints").value(25.0))
                .andExpect(jsonPath("$.monthlyWeightKg[1].month").value("2026-02"))
                .andExpect(jsonPath("$.monthlyWeightKg[1].weightKg").value(6.0))
                .andExpect(jsonPath("$.weightByMaterial[0].material").value("METAL"))
                .andExpect(jsonPath("$.weightByMaterial[0].percent").value(40.0));
    }

    @Test
    @DisplayName("GET /api/v1/dashboard/indicators - intervalo invertido e 400")
    void shouldRejectAnInvertedRange() throws Exception {
        when(getDisposalIndicators.execute(eq(UNIT), any(), any()))
                .thenThrow(new IllegalArgumentException("from cannot be after to"));

        mockMvc.perform(get("/api/v1/dashboard/indicators")
                        .param("from", "2026-05-01")
                        .param("to", "2026-01-01")
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isBadRequest());
    }
}
