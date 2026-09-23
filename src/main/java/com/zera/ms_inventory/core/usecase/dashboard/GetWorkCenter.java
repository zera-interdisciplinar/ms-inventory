package com.zera.ms_inventory.core.usecase.dashboard;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.WorkCenterSummary;

public interface GetWorkCenter {
    WorkCenterSummary execute(UUID unitId, Actor actor);
}
