package com.zera.ms_inventory.core.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.valueobject.CategoryItemCount;
import com.zera.ms_inventory.core.domain.valueobject.InventoryCounts;

/** Leituras agregadas do painel; nao devolvem entidades, so numeros. */
public interface DashboardRepository {

    /** {@code since} e o inicio da janela usada na variacao do estoque. */
    InventoryCounts countsOf(UUID unitId, LocalDateTime since);

    long countDisposalsSince(UUID unitId, LocalDate since);

    /** Motivo da ultima reprovacao de cada item informado, para a Central de Trabalho explicar o porque. */
    Map<UUID, String> lastRejectionReasons(UUID unitId, java.util.List<UUID> itemIds);

    /**
     * Itens ativos agrupados pela categoria do modelo. Sem categoria (rascunho sem modelo) entra
     * numa linha com id nulo — o dashboard precisa mostrar, nao sumir do total.
     */
    List<CategoryItemCount> countActiveItemsByCategory(UUID unitId);
}
