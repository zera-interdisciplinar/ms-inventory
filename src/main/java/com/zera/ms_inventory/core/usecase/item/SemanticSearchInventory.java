package com.zera.ms_inventory.core.usecase.item;

import java.util.List;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Item;

public interface SemanticSearchInventory {
    /** Busca itens da unidade cujo modelo casa semanticamente com {@code query}. */
    List<Item> execute(UUID unitId, String query, int limit);
}
