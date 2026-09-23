package com.zera.ms_inventory.core.usecase.dashboard;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.valueobject.HomeSummary;

public interface GetHomeSummary {
    HomeSummary execute(UUID unitId);
}
