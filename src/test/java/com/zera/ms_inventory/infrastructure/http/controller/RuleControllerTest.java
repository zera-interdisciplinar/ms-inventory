package com.zera.ms_inventory.infrastructure.http.controller;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.domain.exception.RuleNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.RuleKind;
import com.zera.ms_inventory.core.domain.valueobject.RuleLimitUnit;
import com.zera.ms_inventory.core.domain.valueobject.RuleTarget;
import com.zera.ms_inventory.core.domain.valueobject.RuleTargetType;
import com.zera.ms_inventory.core.usecase.rule.CreateRule;
import com.zera.ms_inventory.core.usecase.rule.CreateRuleCommand;
import com.zera.ms_inventory.core.usecase.rule.DeleteRule;
import com.zera.ms_inventory.core.usecase.rule.FindAllRules;
import com.zera.ms_inventory.core.usecase.rule.FindRuleById;
import com.zera.ms_inventory.core.usecase.rule.SetRuleActive;
import com.zera.ms_inventory.core.usecase.rule.UpdateRuleLimit;
import com.zera.ms_inventory.core.usecase.rule.UpdateRuleName;
import com.zera.ms_inventory.core.usecase.rule.UpdateRuleTarget;
import com.zera.ms_inventory.infrastructure.http.handler.GlobalExceptionHandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RuleController.class)
@org.springframework.context.annotation.Import(GlobalExceptionHandler.class)
class RuleControllerTest {

    private static final UUID UNIT = Fixtures.UNIT;

    @Autowired private MockMvc mockMvc;

    @MockitoBean private CreateRule createRule;
    @MockitoBean private FindAllRules findAllRules;
    @MockitoBean private FindRuleById findRuleById;
    @MockitoBean private UpdateRuleName updateRuleName;
    @MockitoBean private UpdateRuleLimit updateRuleLimit;
    @MockitoBean private UpdateRuleTarget updateRuleTarget;
    @MockitoBean private SetRuleActive setRuleActive;
    @MockitoBean private DeleteRule deleteRule;

    private Rule rule(RuleTarget target) {
        return new Rule(UUID.randomUUID(), UNIT, "Garantia vencendo", RuleKind.WARRANTY_EXPIRATION, 30,
                RuleLimitUnit.DAYS, target, true);
    }

    @Test
    @DisplayName("POST /api/v1/rules - deve criar a regra da unidade")
    void shouldCreateRule() throws Exception {
        UUID targetId = UUID.randomUUID();
        when(createRule.execute(any(CreateRuleCommand.class))).thenReturn(rule(RuleTarget.model(targetId)));

        mockMvc.perform(post("/api/v1/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Garantia vencendo\",\"kind\":\"WARRANTY_EXPIRATION\","
                                + "\"limitValue\":30,\"limitUnit\":\"DAYS\",\"targetType\":\"MODEL\","
                                + "\"targetId\":\"" + targetId + "\",\"active\":true}")
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.unitId").value(UNIT.toString()))
                .andExpect(jsonPath("$.targetType").value("MODEL"))
                .andExpect(jsonPath("$.appliesToWholeUnit").value(false));
    }

    /** Sem alvo no corpo, a regra nasce valendo para a unidade inteira. */
    @Test
    @DisplayName("POST /api/v1/rules - deve aceitar regra sem alvo")
    void shouldCreateAWholeUnitRule() throws Exception {
        when(createRule.execute(any(CreateRuleCommand.class))).thenReturn(rule(null));

        mockMvc.perform(post("/api/v1/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Estoque cheio\",\"kind\":\"STOCK_QUANTITY_LIMIT\","
                                + "\"limitValue\":90,\"limitUnit\":\"PERCENT\",\"active\":true}")
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.appliesToWholeUnit").value(true))
                .andExpect(jsonPath("$.targetId").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/v1/rules - deve recusar alvo pela metade")
    void shouldRejectHalfATarget() throws Exception {
        mockMvc.perform(post("/api/v1/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"x\",\"kind\":\"STALE_ITEM\",\"limitValue\":1,"
                                + "\"limitUnit\":\"DAYS\",\"targetType\":\"MODEL\",\"active\":true}")
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/rules - deve recusar limite pela metade")
    void shouldRejectAHalfLimit() throws Exception {
        mockMvc.perform(post("/api/v1/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"x\",\"kind\":\"STALE_ITEM\",\"limitValue\":90,\"active\":true}")
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"x\",\"kind\":\"STALE_ITEM\",\"limitUnit\":\"DAYS\",\"active\":true}")
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/v1/rules/{id}/limit - deve recusar limite pela metade")
    void shouldRejectAHalfLimitOnUpdate() throws Exception {
        mockMvc.perform(patch("/api/v1/rules/{id}/limit", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"limitValue\":90}")
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isBadRequest());
    }

    /** Alvo que nao existe na unidade vira 404, nao regra da unidade inteira. */
    @Test
    @DisplayName("PATCH /api/v1/rules/{id}/target - deve retornar 404 para alvo de outra unidade")
    void shouldReturn404ForATargetFromAnotherUnit() throws Exception {
        UUID id = UUID.randomUUID();
        UUID modelId = UUID.randomUUID();
        when(updateRuleTarget.execute(UNIT, id, RuleTarget.model(modelId)))
                .thenThrow(new com.zera.ms_inventory.core.domain.exception.ModelNotFoundException(modelId));

        mockMvc.perform(patch("/api/v1/rules/{id}/target", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetType\":\"MODEL\",\"targetId\":\"" + modelId + "\"}")
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/rules - deve listar as regras da unidade")
    void shouldListRules() throws Exception {
        when(findAllRules.execute(UNIT)).thenReturn(List.of(rule(null), rule(null)));

        mockMvc.perform(get("/api/v1/rules").header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].unitId").value(UNIT.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/rules/{id} - deve retornar 404 fora da unidade")
    void shouldReturn404ForAnotherUnit() throws Exception {
        UUID id = UUID.randomUUID();
        when(findRuleById.execute(UNIT, id)).thenThrow(new RuleNotFoundException(id));

        mockMvc.perform(get("/api/v1/rules/{id}", id).header("X-Unit-Id", UNIT))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/v1/rules/{id}/target - corpo vazio devolve a regra para a unidade")
    void shouldClearTheTarget() throws Exception {
        UUID id = UUID.randomUUID();
        when(updateRuleTarget.execute(eq(UNIT), eq(id), eq(null))).thenReturn(rule(null));

        mockMvc.perform(patch("/api/v1/rules/{id}/target", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appliesToWholeUnit").value(true));
    }

    @Test
    @DisplayName("PATCH /api/v1/rules/{id}/target - deve apontar para uma categoria")
    void shouldTargetACategory() throws Exception {
        UUID id = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        when(updateRuleTarget.execute(UNIT, id, RuleTarget.category(categoryId)))
                .thenReturn(rule(RuleTarget.category(categoryId)));

        mockMvc.perform(patch("/api/v1/rules/{id}/target", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetType\":\"CATEGORY\",\"targetId\":\"" + categoryId + "\"}")
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetType").value(RuleTargetType.CATEGORY.name()));
    }

    @Test
    @DisplayName("PATCH /api/v1/rules/{id}/activate e /deactivate")
    void shouldActivateAndDeactivate() throws Exception {
        UUID id = UUID.randomUUID();
        when(setRuleActive.execute(eq(UNIT), eq(id), any(Boolean.class))).thenReturn(rule(null));

        mockMvc.perform(patch("/api/v1/rules/{id}/activate", id).header("X-Unit-Id", UNIT))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/v1/rules/{id}/deactivate", id).header("X-Unit-Id", UNIT))
                .andExpect(status().isOk());

        verify(setRuleActive).execute(UNIT, id, true);
        verify(setRuleActive).execute(UNIT, id, false);
    }

    @Test
    @DisplayName("PATCH /api/v1/rules/{id}/limit - deve alterar o limite")
    void shouldUpdateLimit() throws Exception {
        UUID id = UUID.randomUUID();
        when(updateRuleLimit.execute(UNIT, id, 60, RuleLimitUnit.DAYS)).thenReturn(rule(null));

        mockMvc.perform(patch("/api/v1/rules/{id}/limit", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"limitValue\":60,\"limitUnit\":\"DAYS\"}")
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/v1/rules/{id}/name - deve renomear")
    void shouldRename() throws Exception {
        UUID id = UUID.randomUUID();
        when(updateRuleName.execute(UNIT, id, "Novo nome")).thenReturn(rule(null));

        mockMvc.perform(patch("/api/v1/rules/{id}/name", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Novo nome\"}")
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/rules/{id} - deve apagar e retornar 204")
    void shouldDelete() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/rules/{id}", id).header("X-Unit-Id", UNIT))
                .andExpect(status().isNoContent());

        verify(deleteRule).execute(UNIT, id);
    }

    @Test
    @DisplayName("Sem X-Unit-Id a chamada e 400")
    void shouldRequireTheUnitHeader() throws Exception {
        mockMvc.perform(get("/api/v1/rules")).andExpect(status().isBadRequest());
    }
}
