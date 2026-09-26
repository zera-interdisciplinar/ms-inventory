package com.zera.ms_inventory.core.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.valueobject.InventoryCounts;

/** Leituras agregadas do painel; nao devolvem entidades, so numeros. */
public interface DashboardRepository {

    /** {@code since} e o inicio da janela usada na variacao do estoque. */
    InventoryCounts countsOf(UUID unitId, LocalDateTime since);

    long countDisposalsSince(UUID unitId, LocalDate since);

    /** Motivo da ultima reprovacao de cada item informado, para a Central de Trabalho explicar o porque. */
    Map<UUID, String> lastRejectionReasons(UUID unitId, java.util.List<UUID> itemIds);
}
