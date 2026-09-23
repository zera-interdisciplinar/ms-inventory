package com.zera.ms_inventory.core.domain.valueobject;

import java.util.List;

import com.zera.ms_inventory.core.domain.entity.Item;

/**
 * Painel inicial da unidade. {@code occupancyPercent} e {@code activeItemsChangePercent} sao nulos
 * quando nao ha como calcular: sem capacidade configurada e sem estoque anterior, respectivamente.
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
        List<Item> recentItems
) {}
