package com.zera.ms_inventory.core.usecase.item;

import com.zera.ms_inventory.core.domain.entity.Item;

/** {@code created} e falso quando o mesmo id ja tinha sido cadastrado (reenvio do app). */
public record CreateItemResult(Item item, boolean created) {}
