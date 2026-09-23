package com.zera.ms_inventory.core.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Event;
import com.zera.ms_inventory.core.domain.valueobject.EventType;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;

/** Historico do item. Eventos so sao criados e lidos: nao existe update nem delete. */
public interface EventRepository {
    Event save(Event event);
    PageResult<Event> findPageByItem(UUID unitId, UUID itemId, Pagination pagination);
    List<Event> findAllByItem(UUID unitId, UUID itemId);
    /** Ultimo evento do tipo; a restauracao usa o fromStatus do REMOVED para saber de onde voltar. */
    Optional<Event> findLastByItemAndType(UUID unitId, UUID itemId, EventType type);
}
