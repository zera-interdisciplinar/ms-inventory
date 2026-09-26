package com.zera.ms_inventory.core.domain.valueobject;

/**
 * O que a regra vigia. A avaliacao de cada tipo entra na ZERA-255; aqui eles ja existem para que
 * a unidade possa configura-los.
 */
public enum RuleKind {
    WARRANTY_EXPIRATION,
    LIFESPAN_EXPIRATION,
    USAGE_INTENSITY_LIMIT,
    STOCK_QUANTITY_LIMIT,
    TIME_IN_STOCK_LIMIT,
    STALE_ITEM,
    /** Material reciclavel enviado ao aterro; avaliada no momento do descarte. */
    RECYCLABLE_TO_LANDFILL,
    /** Data prevista de quebra chegando, calculada pelo sistema preditivo. */
    PREDICTED_FAILURE;

    /** Limites relativos sao os unicos que fazem sentido em PERCENT. */
    public boolean acceptsPercent() {
        return this == STOCK_QUANTITY_LIMIT;
    }
}
