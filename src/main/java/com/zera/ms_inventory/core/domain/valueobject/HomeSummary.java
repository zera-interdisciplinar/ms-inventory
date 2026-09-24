package com.zera.ms_inventory.core.domain.valueobject;

import com.zera.ms_inventory.core.domain.entity.Item;

/**
 * Painel inicial da unidade. {@code occupancyPercent} e {@code activeItemsChangePercent} sao nulos
 * quando nao ha como calcular: sem capacidade configurada e sem estoque anterior, respectivamente.
 * {@code recentItems} e paginado para o app poder virar a lista em "ver todos" sem endpoint novo.
 */
public record HomeSummary(
        long activeItems,
        Double activeItemsChangePercent,
        Integer stockCapacity,
        Double occupancyPercent,
        long pendingApproval,
        long inMaintenance,
        long awaitingEvaluation,
        long disposalsInWindow,
        int windowDays,
        PageResult<Item> recentItems
) {}
