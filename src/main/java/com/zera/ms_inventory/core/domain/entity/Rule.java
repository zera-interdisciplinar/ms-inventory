package com.zera.ms_inventory.core.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.valueobject.RuleKind;
import com.zera.ms_inventory.core.domain.valueobject.RuleLimitUnit;
import com.zera.ms_inventory.core.domain.valueobject.RuleTarget;

/**
 * Regra de alerta configurada pela unidade. Sem alvo, vale para a unidade inteira; com alvo, so
 * para os itens daquele modelo ou categoria. O limite e opcional, mas vem inteiro: valor e
 * unidade juntos, ou nenhum dos dois.
 */
public class Rule {

    private final UUID id;
    private final UUID unitId;
    private String name;
    private RuleKind kind;
    private Integer limitValue;
    private RuleLimitUnit limitUnit;
    private RuleTarget target;
    private boolean active;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Rule(UUID id, UUID unitId, String name, RuleKind kind, Integer limitValue, RuleLimitUnit limitUnit,
                RuleTarget target, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        if (unitId == null) {
            throw new IllegalArgumentException("unitId is required");
        }
        if (kind == null) {
            throw new IllegalArgumentException("kind is required");
        }
        validateLimit(kind, limitValue, limitUnit);
        this.id = id != null ? id : UUID.randomUUID();
        this.unitId = unitId;
        this.name = name;
        this.kind = kind;
        this.limitValue = limitValue;
        this.limitUnit = limitUnit;
        this.target = target;
        this.active = active;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public Rule(UUID id, UUID unitId, String name, RuleKind kind, Integer limitValue, RuleLimitUnit limitUnit,
                RuleTarget target, boolean active) {
        this(id, unitId, name, kind, limitValue, limitUnit, target, active, null, null);
    }

    /**
     * PERCENT so vale para limite relativo: garantia em 20% nao quer dizer nada, e deixar passar
     * geraria alerta com numero sem sentido.
     */
    private static void validateLimit(RuleKind kind, Integer limitValue, RuleLimitUnit limitUnit) {
        // valor sem unidade nao da para interpretar, e unidade sem valor nao limita nada
        if ((limitValue == null) != (limitUnit == null)) {
            throw new IllegalArgumentException("inform both limitValue and limitUnit, or neither");
        }
        if (limitValue != null && limitValue < 0) {
            throw new IllegalArgumentException("limitValue cannot be negative");
        }
        if (limitUnit == RuleLimitUnit.PERCENT && !kind.acceptsPercent()) {
            throw new IllegalArgumentException("PERCENT limit is not valid for " + kind);
        }
    }

    public void rename(String name) {
        this.name = name;
        touch();
    }

    public void changeLimit(Integer limitValue, RuleLimitUnit limitUnit) {
        validateLimit(kind, limitValue, limitUnit);
        this.limitValue = limitValue;
        this.limitUnit = limitUnit;
        touch();
    }

    /** Alvo nulo devolve a regra para a unidade inteira. */
    public void changeTarget(RuleTarget target) {
        this.target = target;
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

    public boolean appliesToWholeUnit() {
        return target == null;
    }

    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUnitId() {
        return unitId;
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

    public RuleTarget getTarget() {
        return target;
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
}
