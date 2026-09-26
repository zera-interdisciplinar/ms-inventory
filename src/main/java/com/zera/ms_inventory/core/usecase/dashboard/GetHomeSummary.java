package com.zera.ms_inventory.core.usecase.dashboard;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.valueobject.HomeSummary;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;

public interface GetHomeSummary {
    /** A paginacao vale para os itens recentes; os contadores sao sempre da unidade inteira. */
    HomeSummary execute(UUID unitId, Pagination recentItemsPage);
}
