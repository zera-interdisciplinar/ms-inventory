package com.zera.ms_inventory.infrastructure.security;

/**
 * Expressoes SpEL reutilizaveis para {@code @PreAuthorize}.
 *
 * <p>Regra da v1: escrita exige {@code MANAGER}; leitura exige apenas autenticacao (garantido pelo
 * {@code SecurityFilterChain}). TODO: revisar se operacoes de inventario do dia a dia deveriam
 * ficar acessiveis a {@code EMPLOYEE}.
 */
public final class Authz {

    public static final String MANAGER = "hasRole('MANAGER')";

    private Authz() {}
}
