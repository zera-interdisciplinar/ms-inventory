package com.zera.ms_inventory.infrastructure.persistence.neo4j.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node("Rule")
public class RuleNode {

    @Id
    private UUID id;

    private String name;

    private String kind;

    private Integer limitValue;

    private String limitUnit;

    private String targetType;

    private UUID targetId;

    private boolean active;

    @Property("createdAt")
    private LocalDateTime createdAt;

    @Property("updatedAt")
    private LocalDateTime updatedAt;

    public RuleNode() {
    }

    public RuleNode(UUID id, String name, String kind, Integer limitValue, String limitUnit, String targetType,
                     UUID targetId, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
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

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getKind() {
        return kind;
    }

    public Integer getLimitValue() {
        return limitValue;
    }

    public String getLimitUnit() {
        return limitUnit;
    }

    public String getTargetType() {
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
}
