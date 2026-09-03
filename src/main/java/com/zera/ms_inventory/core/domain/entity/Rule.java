package com.zera.ms_inventory.core.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.valueobject.RuleKind;
import com.zera.ms_inventory.core.domain.valueobject.RuleLimitUnit;
import com.zera.ms_inventory.core.domain.valueobject.RuleTargetType;

public class Rule {
    private final UUID id;
    private String name;
    private RuleKind kind;
    private Integer limitValue;
    private RuleLimitUnit limitUnit;
    private RuleTargetType targetType;
    private UUID targetId;
    private boolean active;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Rule(UUID id, String name, RuleKind kind, Integer limitValue, RuleLimitUnit limitUnit,
                RuleTargetType targetType, UUID targetId, boolean active, LocalDateTime createdAt,
                LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.kind = kind;
        this.limitValue = limitValue;
        this.limitUnit = limitUnit;
        this.targetType = targetType;
        this.targetId = targetId;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Rule(UUID id, String name, RuleKind kind, Integer limitValue, RuleLimitUnit limitUnit,
                RuleTargetType targetType, UUID targetId, boolean active) {
        this.id = id;
        this.name = name;
        this.kind = kind;
        this.limitValue = limitValue;
        this.limitUnit = limitUnit;
        this.targetType = targetType;
        this.targetId = targetId;
        this.active = active;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // -------------------------------------------------

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public RuleKind getKind() {
        return kind;
    }

    public Integer getLimitValue() {
        return limitValue;
    }

    public RuleLimitUnit getLimitUnit() {
        return limitUnit;
    }

    public RuleTargetType getTargetType() {
        return targetType;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // -------------------------------------

    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    public void rename(String newName) {
        this.name = newName;
        touch();
    }

    public void changeLimit(Integer newLimitValue, RuleLimitUnit newLimitUnit) {
        this.limitValue = newLimitValue;
        this.limitUnit = newLimitUnit;
        touch();
    }

    public void changeTarget(RuleTargetType newTargetType, UUID newTargetId) {
        this.targetType = newTargetType;
        this.targetId = newTargetId;
        touch();
    }

    public void activate() {
        this.active = true;
        touch();
    }

    public void deactivate() {
        this.active = false;
        touch();
    }
}
