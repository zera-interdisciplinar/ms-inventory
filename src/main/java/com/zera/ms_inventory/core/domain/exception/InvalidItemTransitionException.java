package com.zera.ms_inventory.core.domain.exception;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;

/** Transicao fora da maquina de estados do item; a API traduz para 409. */
public class InvalidItemTransitionException extends RuntimeException {

    public InvalidItemTransitionException(UUID itemId, ItemStatus from, ItemStatus to) {
        super("Item " + itemId + " cannot go from " + from + " to " + to
                + "; allowed: " + from.allowedTransitions());
    }
}
