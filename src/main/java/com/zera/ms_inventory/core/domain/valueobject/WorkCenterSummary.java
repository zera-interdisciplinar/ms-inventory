package com.zera.ms_inventory.core.domain.valueobject;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Item;

/**
 * Central de Trabalho: o que a pessoa logada precisa resolver. Rascunhos e reprovados sao os dela;
 * os danificados sem destino sao da unidade, porque qualquer um pode registrar o descarte.
 */
public record WorkCenterSummary(
        List<Item> drafts,
        List<Item> rejected,
        Map<UUID, String> rejectionReasons,
        List<Item> damagedWithoutDestination,
        long inMaintenance,
        long awaitingEvaluation
) {}
