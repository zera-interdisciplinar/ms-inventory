package com.zera.ms_inventory.infrastructure.http.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.domain.exception.RuleNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.RuleKind;
import com.zera.ms_inventory.core.domain.valueobject.RuleLimitUnit;
import com.zera.ms_inventory.core.domain.valueobject.RuleTargetType;
import com.zera.ms_inventory.core.usecase.rule.ActivateRule;
import com.zera.ms_inventory.core.usecase.rule.CreateRule;
import com.zera.ms_inventory.core.usecase.rule.DeactivateRule;
import com.zera.ms_inventory.core.usecase.rule.DeleteRule;
import com.zera.ms_inventory.core.usecase.rule.FindAllRules;
import com.zera.ms_inventory.core.usecase.rule.FindRuleById;
import com.zera.ms_inventory.core.usecase.rule.UpdateRuleLimit;
import com.zera.ms_inventory.core.usecase.rule.UpdateRuleName;
import com.zera.ms_inventory.core.usecase.rule.UpdateRuleTarget;
import com.zera.ms_inventory.infrastructure.http.handler.GlobalExceptionHandler;
import com.zera.ms_inventory.infrastructure.http.request.CreateRuleRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateRuleLimitRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateRuleNameRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateRuleTargetRequest;

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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private CreateRule createRule;
    @MockitoBean private FindAllRules findAllRules;
    @MockitoBean private FindRuleById findRuleById;
    @MockitoBean private UpdateRuleName updateRuleName;
    @MockitoBean private UpdateRuleLimit updateRuleLimit;
    @MockitoBean private UpdateRuleTarget updateRuleTarget;
    @MockitoBean private ActivateRule activateRule;
    @MockitoBean private DeactivateRule deactivateRule;
    @MockitoBean private DeleteRule deleteRule;

    @Test
    @DisplayName("POST /api/v1/rules - deve criar a regra e retornar 201")
    void shouldCreateRule() throws Exception {
        UUID id = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        Rule rule = new Rule(id, "Warranty check", RuleKind.WARRANTY_EXPIRATION, 12, RuleLimitUnit.MONTHS,
                RuleTargetType.MODEL, targetId, true);
        when(createRule.execute(eq("Warranty check"), eq(RuleKind.WARRANTY_EXPIRATION), eq(12),
                eq(RuleLimitUnit.MONTHS), eq(RuleTargetType.MODEL), eq(targetId), eq(true),
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(rule);

        CreateRuleRequest request = new CreateRuleRequest("Warranty check", RuleKind.WARRANTY_EXPIRATION, 12,
                RuleLimitUnit.MONTHS, RuleTargetType.MODEL, targetId, true);

        mockMvc.perform(post("/api/v1/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Warranty check"));
    }

    @Test
    @DisplayName("POST /api/v1/rules - deve retornar 400 quando o nome estiver em branco")
    void shouldReturn400WhenNameIsBlank() throws Exception {
        CreateRuleRequest request = new CreateRuleRequest("", RuleKind.WARRANTY_EXPIRATION, 12,
                RuleLimitUnit.MONTHS, RuleTargetType.MODEL, UUID.randomUUID(), true);

        mockMvc.perform(post("/api/v1/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/rules - deve listar todas as regras")
    void shouldFindAllRules() throws Exception {
        Rule rule = new Rule(UUID.randomUUID(), "Warranty check", RuleKind.WARRANTY_EXPIRATION, 12,
                RuleLimitUnit.MONTHS, RuleTargetType.MODEL, UUID.randomUUID(), true);
        when(findAllRules.execute()).thenReturn(List.of(rule));

        mockMvc.perform(get("/api/v1/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Warranty check"));
    }

    @Test
    @DisplayName("GET /api/v1/rules/{id} - deve retornar a regra")
    void shouldFindRuleById() throws Exception {
        UUID id = UUID.randomUUID();
        Rule rule = new Rule(id, "Warranty check", RuleKind.WARRANTY_EXPIRATION, 12, RuleLimitUnit.MONTHS,
                RuleTargetType.MODEL, UUID.randomUUID(), true);
        when(findRuleById.execute(id)).thenReturn(rule);

        mockMvc.perform(get("/api/v1/rules/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/rules/{id} - deve retornar 404 quando a regra não existir")
    void shouldReturn404WhenRuleDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        when(findRuleById.execute(id)).thenThrow(new RuleNotFoundException(id));

        mockMvc.perform(get("/api/v1/rules/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/v1/rules/{id}/name - deve renomear a regra")
    void shouldRenameRule() throws Exception {
        UUID id = UUID.randomUUID();
        Rule rule = new Rule(id, "Lifespan check", RuleKind.WARRANTY_EXPIRATION, 12, RuleLimitUnit.MONTHS,
                RuleTargetType.MODEL, UUID.randomUUID(), true);
        when(updateRuleName.execute(id, "Lifespan check")).thenReturn(rule);

        mockMvc.perform(patch("/api/v1/rules/{id}/name", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateRuleNameRequest("Lifespan check"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lifespan check"));
    }

    @Test
    @DisplayName("PATCH /api/v1/rules/{id}/limit - deve atualizar o limite")
    void shouldUpdateRuleLimit() throws Exception {
        UUID id = UUID.randomUUID();
        Rule rule = new Rule(id, "Warranty check", RuleKind.WARRANTY_EXPIRATION, 24, RuleLimitUnit.MONTHS,
                RuleTargetType.MODEL, UUID.randomUUID(), true);
        when(updateRuleLimit.execute(id, 24, RuleLimitUnit.MONTHS)).thenReturn(rule);

        mockMvc.perform(patch("/api/v1/rules/{id}/limit", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateRuleLimitRequest(24, RuleLimitUnit.MONTHS))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.limitValue").value(24));
    }

    @Test
    @DisplayName("PATCH /api/v1/rules/{id}/target - deve atualizar o alvo")
    void shouldUpdateRuleTarget() throws Exception {
        UUID id = UUID.randomUUID();
        UUID newTargetId = UUID.randomUUID();
        Rule rule = new Rule(id, "Warranty check", RuleKind.WARRANTY_EXPIRATION, 12, RuleLimitUnit.MONTHS,
                RuleTargetType.CATEGORY, newTargetId, true);
        when(updateRuleTarget.execute(id, RuleTargetType.CATEGORY, newTargetId)).thenReturn(rule);

        mockMvc.perform(patch("/api/v1/rules/{id}/target", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateRuleTargetRequest(RuleTargetType.CATEGORY, newTargetId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetType").value("CATEGORY"));
    }

    @Test
    @DisplayName("PATCH /api/v1/rules/{id}/activate - deve ativar a regra")
    void shouldActivateRule() throws Exception {
        UUID id = UUID.randomUUID();
        Rule rule = new Rule(id, "Warranty check", RuleKind.WARRANTY_EXPIRATION, 12, RuleLimitUnit.MONTHS,
                RuleTargetType.MODEL, UUID.randomUUID(), true);
        when(activateRule.execute(id)).thenReturn(rule);

        mockMvc.perform(patch("/api/v1/rules/{id}/activate", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("PATCH /api/v1/rules/{id}/deactivate - deve desativar a regra")
    void shouldDeactivateRule() throws Exception {
        UUID id = UUID.randomUUID();
        Rule rule = new Rule(id, "Warranty check", RuleKind.WARRANTY_EXPIRATION, 12, RuleLimitUnit.MONTHS,
                RuleTargetType.MODEL, UUID.randomUUID(), false);
        when(deactivateRule.execute(id)).thenReturn(rule);

        mockMvc.perform(patch("/api/v1/rules/{id}/deactivate", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @DisplayName("DELETE /api/v1/rules/{id} - deve remover a regra e retornar 204")
    void shouldDeleteRule() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/rules/{id}", id))
                .andExpect(status().isNoContent());

        verify(deleteRule).execute(id);
    }
}
