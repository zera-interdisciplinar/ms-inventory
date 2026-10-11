package com.zera.ms_inventory.core.domain.exception;

import java.util.UUID;

/** Segunda tentativa de cancelar o mesmo descarte; a API traduz para 409. */
public class DisposalAlreadyCancelledException extends RuntimeException {
    public DisposalAlreadyCancelledException(UUID id) {
        super("Disposal already cancelled: " + id);
    }
}
