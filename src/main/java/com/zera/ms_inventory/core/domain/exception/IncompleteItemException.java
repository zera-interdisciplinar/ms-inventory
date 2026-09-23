package com.zera.ms_inventory.core.domain.exception;

import java.util.List;
import java.util.UUID;

/**
 * Item enviado para aprovacao sem os campos que o cadastro exige. Carrega a lista do que falta
 * para a tela do app marcar os campos, e a API responde 422.
 */
public class IncompleteItemException extends RuntimeException {

    private final List<String> missingFields;

    public IncompleteItemException(UUID itemId, List<String> missingFields) {
        super("Item " + itemId + " is missing required fields: " + String.join(", ", missingFields));
        this.missingFields = List.copyOf(missingFields);
    }

    public List<String> getMissingFields() {
        return missingFields;
    }
}
