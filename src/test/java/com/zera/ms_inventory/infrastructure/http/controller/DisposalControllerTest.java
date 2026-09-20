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
import tools.jackson.databind.ObjectMapper;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Disposal;
import com.zera.ms_inventory.core.domain.exception.DisposalNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.DestinationType;
import com.zera.ms_inventory.core.domain.valueobject.DisposedItem;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
import com.zera.ms_inventory.core.usecase.disposal.CorrectDisposalDestination;
import com.zera.ms_inventory.core.usecase.disposal.CreateDisposal;
import com.zera.ms_inventory.core.usecase.disposal.CreateDisposalCommand;
import com.zera.ms_inventory.core.usecase.disposal.FindDisposalById;
import com.zera.ms_inventory.core.usecase.disposal.ListDisposals;
import com.zera.ms_inventory.infrastructure.http.handler.GlobalExceptionHandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DisposalController.class)
@org.springframework.context.annotation.Import(GlobalExceptionHandler.class)
class DisposalControllerTest {

    private static final UUID UNIT = Fixtures.UNIT;
    private static final UUID OPERATOR_ID = UUID.fromString("00000000-0000-0000-0000-0000000000e1");

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private CreateDisposal createDisposal;
    @MockitoBean private ListDisposals listDisposals;
    @MockitoBean private FindDisposalById findDisposalById;
    @MockitoBean private CorrectDisposalDestination correctDisposalDestination;

    private Disposal sample(DestinationType destination) {
        return Disposal.register(UNIT, destination, "places/abc", "Ecoponto Central", LocalDate.now(), null,
                List.of(new DisposedItem(UUID.randomUUID(), "100001", "Notebook", 2.5),
                        new DisposedItem(UUID.randomUUID(), "100002", "Monitor", 1.5)), Fixtures.OPERATOR);
    }

    @Test
    @DisplayName("POST /api/v1/disposals - deve registrar o descarte com o peso total")
    void shouldCreateDisposal() throws Exception {
        when(createDisposal.execute(any(CreateDisposalCommand.class)))
                .thenReturn(sample(DestinationType.RECYCLING));

        mockMvc.perform(post("/api/v1/disposals")
                        .principal(new TestingAuthenticationToken(OPERATOR_ID.toString(), null, "ROLE_EMPLOYEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new java.util.HashMap<>(java.util.Map.of(
                                "destination", "RECYCLING",
                                "placeId", "places/abc",
                                "itemIds", List.of(UUID.randomUUID().toString())))))
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.destination").value("RECYCLING"))
                .andExpect(jsonPath("$.totalWeightKg").value(4.0))
                .andExpect(jsonPath("$.items[0].displayCode").value("100001"))
                .andExpect(jsonPath("$.items[0].weightKg").value(2.5));
    }

    @Test
    @DisplayName("POST /api/v1/disposals - deve exigir destino e ao menos um item")
    void shouldRejectAnIncompleteDisposal() throws Exception {
        mockMvc.perform(post("/api/v1/disposals")
                        .principal(new TestingAuthenticationToken(OPERATOR_ID.toString(), null, "ROLE_EMPLOYEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemIds\":[]}")
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isBadRequest());
    }

    /** Sem agendamento no sistema, data futura e recusada ja na borda. */
    @Test
    @DisplayName("POST /api/v1/disposals - deve recusar data no futuro")
    void shouldRejectAFutureDate() throws Exception {
        mockMvc.perform(post("/api/v1/disposals")
                        .principal(new TestingAuthenticationToken(OPERATOR_ID.toString(), null, "ROLE_EMPLOYEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"destination\":\"RECYCLING\",\"disposedAt\":\""
                                + LocalDate.now().plusDays(1) + "\",\"itemIds\":[\"" + UUID.randomUUID() + "\"]}")
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/disposals - deve listar paginado")
    void shouldListDisposals() throws Exception {
        when(listDisposals.execute(UNIT, new Pagination(0, 20)))
                .thenReturn(new PageResult<>(List.of(sample(DestinationType.DONATION)), 0, 20, 1));

        mockMvc.perform(get("/api/v1/disposals").header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].destination").value("DONATION"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/disposals/{id} - deve retornar 404 fora da unidade")
    void shouldReturn404ForAnotherUnit() throws Exception {
        UUID id = UUID.randomUUID();
        when(findDisposalById.execute(UNIT, id)).thenThrow(new DisposalNotFoundException(id));

        mockMvc.perform(get("/api/v1/disposals/{id}", id).header("X-Unit-Id", UNIT))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/v1/disposals/{id} - deve corrigir o destino")
    void shouldCorrectDestination() throws Exception {
        UUID id = UUID.randomUUID();
        when(correctDisposalDestination.execute(UNIT, id, DestinationType.RECYCLING))
                .thenReturn(sample(DestinationType.RECYCLING));

        mockMvc.perform(patch("/api/v1/disposals/{id}", id)
                        .principal(new TestingAuthenticationToken(OPERATOR_ID.toString(), null, "ROLE_EMPLOYEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"destination\":\"RECYCLING\"}")
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destination").value("RECYCLING"));
    }
}
