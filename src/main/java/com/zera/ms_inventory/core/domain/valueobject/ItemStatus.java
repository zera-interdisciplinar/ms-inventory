package com.zera.ms_inventory.core.domain.valueobject;

import java.util.Map;
import java.util.Set;

/**
 * Estados do item no fluxo v1 (Figma "Section 4"). As transicoes validas moram aqui: o dominio
 * recusa qualquer outra, e a API responde 409. {@link #DISPOSED} e terminal; {@link #REMOVED} e
 * alcancavel de qualquer estado menos o descarte (ZERA-247).
 */
public enum ItemStatus {
    /** Cadastro incompleto salvo pelo operario; ainda nao entrou no estoque. */
    DRAFT,
    /** Aguardando o gestor aprovar o cadastro feito pelo operario. */
    PENDING_APPROVAL,
    /** Reprovado pelo gestor; pode ser corrigido e reenviado. */
    REJECTED,
    IN_STOCK,
    IN_MAINTENANCE,
    /** Manutencao concluida, esperando alguem avaliar em que condicao o item voltou. */
    AWAITING_EVALUATION,
    DISPOSED,
    /** Remocao logica: sai das listagens, mas o gestor consegue restaurar. */
    REMOVED;

    private static final Map<ItemStatus, Set<ItemStatus>> TRANSITIONS = Map.of(
            DRAFT, Set.of(PENDING_APPROVAL, IN_STOCK, REMOVED),
            PENDING_APPROVAL, Set.of(IN_STOCK, REJECTED, REMOVED),
            REJECTED, Set.of(PENDING_APPROVAL, IN_STOCK, REMOVED),
            IN_STOCK, Set.of(IN_MAINTENANCE, DISPOSED, REMOVED),
            IN_MAINTENANCE, Set.of(AWAITING_EVALUATION, REMOVED),
            AWAITING_EVALUATION, Set.of(IN_STOCK, DISPOSED, REMOVED),
            DISPOSED, Set.of(),
            REMOVED, Set.of(DRAFT, PENDING_APPROVAL, REJECTED, IN_STOCK, IN_MAINTENANCE, AWAITING_EVALUATION));

    public boolean canTransitionTo(ItemStatus target) {
        return target != null && TRANSITIONS.get(this).contains(target);
    }

    public Set<ItemStatus> allowedTransitions() {
        return TRANSITIONS.get(this);
    }

    /** Estados que o item ocupa dentro do estoque, usados como padrao das listagens. */
    public boolean isActive() {
        return this != DISPOSED && this != REMOVED;
    }
}
