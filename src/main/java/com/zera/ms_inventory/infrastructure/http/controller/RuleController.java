package com.zera.ms_inventory.infrastructure.http.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.usecase.rule.ActivateRule;
import com.zera.ms_inventory.core.usecase.rule.CreateRule;
import com.zera.ms_inventory.core.usecase.rule.DeactivateRule;
import com.zera.ms_inventory.core.usecase.rule.DeleteRule;
import com.zera.ms_inventory.core.usecase.rule.FindAllRules;
import com.zera.ms_inventory.core.usecase.rule.FindRuleById;
import com.zera.ms_inventory.core.usecase.rule.UpdateRuleLimit;
import com.zera.ms_inventory.core.usecase.rule.UpdateRuleName;
import com.zera.ms_inventory.core.usecase.rule.UpdateRuleTarget;
import com.zera.ms_inventory.infrastructure.http.request.CreateRuleRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateRuleLimitRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateRuleNameRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateRuleTargetRequest;

@RestController
@RequestMapping("/api/v1/rules")
public class RuleController {

    private final CreateRule createRule;
    private final FindAllRules findAllRules;
    private final FindRuleById findRuleById;
    private final UpdateRuleName updateRuleName;
    private final UpdateRuleLimit updateRuleLimit;
    private final UpdateRuleTarget updateRuleTarget;
    private final ActivateRule activateRule;
    private final DeactivateRule deactivateRule;
    private final DeleteRule deleteRule;

    public RuleController(CreateRule createRule,
                           FindAllRules findAllRules,
                           FindRuleById findRuleById,
                           UpdateRuleName updateRuleName,
                           UpdateRuleLimit updateRuleLimit,
                           UpdateRuleTarget updateRuleTarget,
                           ActivateRule activateRule,
                           DeactivateRule deactivateRule,
                           DeleteRule deleteRule) {
        this.createRule = createRule;
        this.findAllRules = findAllRules;
        this.findRuleById = findRuleById;
        this.updateRuleName = updateRuleName;
        this.updateRuleLimit = updateRuleLimit;
        this.updateRuleTarget = updateRuleTarget;
        this.activateRule = activateRule;
        this.deactivateRule = deactivateRule;
        this.deleteRule = deleteRule;
    }

    @PostMapping
    public ResponseEntity<Rule> create(@RequestBody @Valid CreateRuleRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Rule created = createRule.execute(request.name(), request.kind(), request.limitValue(), request.limitUnit(),
                request.targetType(), request.targetId(), request.active(), now, now);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Rule>> findAll() {
        return ResponseEntity.ok(findAllRules.execute());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rule> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(findRuleById.execute(id));
    }

    @PatchMapping("/{id}/name")
    public ResponseEntity<Rule> rename(@PathVariable UUID id, @RequestBody @Valid UpdateRuleNameRequest request) {
        return ResponseEntity.ok(updateRuleName.execute(id, request.name()));
    }

    @PatchMapping("/{id}/limit")
    public ResponseEntity<Rule> updateLimit(@PathVariable UUID id, @RequestBody @Valid UpdateRuleLimitRequest request) {
        return ResponseEntity.ok(updateRuleLimit.execute(id, request.limitValue(), request.limitUnit()));
    }

    @PatchMapping("/{id}/target")
    public ResponseEntity<Rule> updateTarget(@PathVariable UUID id, @RequestBody @Valid UpdateRuleTargetRequest request) {
        return ResponseEntity.ok(updateRuleTarget.execute(id, request.targetType(), request.targetId()));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Rule> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(activateRule.execute(id));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Rule> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(deactivateRule.execute(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteRule.execute(id);
        return ResponseEntity.noContent().build();
    }
}
