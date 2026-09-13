package com.zera.ms_inventory.core.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * Quem executa a acao: o usuario do token (sub), o seu papel e o nome exibido (claim name, pode
 * faltar enquanto o ms-administrative-core nao o emitir).
 */
public record Actor(UUID userId, ActorRole role, String name) {

    public Actor {
        Objects.requireNonNull(userId, "userId is required");
        Objects.requireNonNull(role, "role is required");
    }

    public Actor(UUID userId, ActorRole role) {
        this(userId, role, null);
    }

    public boolean isManager() {
        return role == ActorRole.MANAGER;
    }
}
