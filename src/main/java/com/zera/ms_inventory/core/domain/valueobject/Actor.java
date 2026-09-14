package com.zera.ms_inventory.core.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

/** Quem executa a acao: o usuario do token (sub) e o seu papel. */
public record Actor(UUID userId, ActorRole role) {

    public Actor {
        Objects.requireNonNull(userId, "userId is required");
        Objects.requireNonNull(role, "role is required");
    }

    public boolean isManager() {
        return role == ActorRole.MANAGER;
    }
}
