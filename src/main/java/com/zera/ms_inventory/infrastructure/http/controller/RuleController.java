package com.zera.ms_inventory.infrastructure.http.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zera.ms_inventory.core.usecase.rule.CreateRule;
import com.zera.ms_inventory.core.usecase.rule.DeleteRule;
import com.zera.ms_inventory.core.usecase.rule.FindAllRules;
import com.zera.ms_inventory.core.usecase.rule.FindRuleById;
import com.zera.ms_inventory.core.usecase.rule.SetRuleActive;
import com.zera.ms_inventory.core.usecase.rule.UpdateRuleLimit;
import com.zera.ms_inventory.core.usecase.rule.UpdateRuleName;
import com.zera.ms_inventory.core.usecase.rule.UpdateRuleTarget;
import com.zera.ms_inventory.infrastructure.http.request.CreateRuleRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateRuleLimitRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateRuleNameRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateRuleTargetRequest;
import com.zera.ms_inventory.infrastructure.http.response.RuleResponse;
import com.zera.ms_inventory.infrastructure.security.Authz;

/** Regras sao configuracao da unidade: so o gestor edita, qualquer autenticado consulta. */
@RestController
@RequestMapping("/api/v1/rules")
@PreAuthorize(Authz.MANAGER)
public class RuleController {

    private final CreateRule createRule;
    private final FindAllRules findAllRules;
    private final FindRuleById findRuleById;
    private final UpdateRuleName updateRuleName;
    private final UpdateRuleLimit updateRuleLimit;
    private final UpdateRuleTarget updateRuleTarget;
    private final SetRuleActive setRuleActive;
    private final DeleteRule deleteRule;

    public RuleController(CreateRule createRule,
                          FindAllRules findAllRules,
                          FindRuleById findRuleById,
                          UpdateRuleName updateRuleName,
                          UpdateRuleLimit updateRuleLimit,
                          UpdateRuleTarget updateRuleTarget,
                          SetRuleActive setRuleActive,
                          DeleteRule deleteRule) {
        this.createRule = createRule;
        this.findAllRules = findAllRules;
        this.findRuleById = findRuleById;
        this.updateRuleName = updateRuleName;
        this.updateRuleLimit = updateRuleLimit;
        this.updateRuleTarget = updateRuleTarget;
        this.setRuleActive = setRuleActive;
        this.deleteRule = deleteRule;
    }

    @PostMapping
    public ResponseEntity<RuleResponse> create(@RequestHeader("X-Unit-Id") UUID unitId,
                                               @RequestBody @Valid CreateRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RuleResponse.from(createRule.execute(request.toCommand(unitId))));
    }

    /** A primeira listagem da unidade cria as regras padrao. */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RuleResponse>> findAll(@RequestHeader("X-Unit-Id") UUID unitId) {
        return ResponseEntity.ok(findAllRules.execute(unitId).stream().map(RuleResponse::from).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RuleResponse> findById(@RequestHeader("X-Unit-Id") UUID unitId,
                                                 @PathVariable UUID id) {
        return ResponseEntity.ok(RuleResponse.from(findRuleById.execute(unitId, id)));
    }

    @PatchMapping("/{id}/name")
    public ResponseEntity<RuleResponse> rename(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id,
                                               @RequestBody @Valid UpdateRuleNameRequest request) {
        return ResponseEntity.ok(RuleResponse.from(updateRuleName.execute(unitId, id, request.name())));
    }

    @PatchMapping("/{id}/limit")
    public ResponseEntity<RuleResponse> updateLimit(@RequestHeader("X-Unit-Id") UUID unitId,
                                                    @PathVariable UUID id,
                                                    @RequestBody @Valid UpdateRuleLimitRequest request) {
        return ResponseEntity.ok(RuleResponse.from(
                updateRuleLimit.execute(unitId, id, request.limitValue(), request.limitUnit())));
    }

    /** Sem alvo no corpo, a regra volta a valer para a unidade inteira. */
    @PatchMapping("/{id}/target")
    public ResponseEntity<RuleResponse> updateTarget(@RequestHeader("X-Unit-Id") UUID unitId,
                                                     @PathVariable UUID id,
                                                     @RequestBody @Valid UpdateRuleTargetRequest request) {
        return ResponseEntity.ok(RuleResponse.from(
                updateRuleTarget.execute(unitId, id, request.toTarget())));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<RuleResponse> activate(@RequestHeader("X-Unit-Id") UUID unitId,
                                                 @PathVariable UUID id) {
        return ResponseEntity.ok(RuleResponse.from(setRuleActive.execute(unitId, id, true)));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<RuleResponse> deactivate(@RequestHeader("X-Unit-Id") UUID unitId,
                                                   @PathVariable UUID id) {
        return ResponseEntity.ok(RuleResponse.from(setRuleActive.execute(unitId, id, false)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id) {
        deleteRule.execute(unitId, id);
        return ResponseEntity.noContent().build();
    }
}
