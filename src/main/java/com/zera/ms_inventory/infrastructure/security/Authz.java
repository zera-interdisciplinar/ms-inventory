package com.zera.ms_inventory.infrastructure.security;

/**
 * Expressoes SpEL reutilizaveis para {@code @PreAuthorize}.
 *
 * <p>Matriz da v1: {@code EMPLOYEE} cadastra e edita itens e modelos; {@code MANAGER} faz tudo isso e
 * ainda cuida de categorias, regras, exclusoes e transferencia de unidade. Leitura exige apenas
 * autenticacao (garantido pelo {@code SecurityFilterChain}).
 */
public final class Authz {

    public static final String MANAGER = "hasRole('MANAGER')";

    /** Operacoes de inventario do dia a dia: operario e gestor. */
    public static final String INVENTORY_OPERATOR = "hasAnyRole('MANAGER', 'EMPLOYEE')";

    private Authz() {}
}
