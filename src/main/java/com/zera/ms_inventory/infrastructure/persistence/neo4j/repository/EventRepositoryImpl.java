package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.zera.ms_inventory.core.domain.entity.Event;
import com.zera.ms_inventory.core.domain.valueobject.EventType;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
import com.zera.ms_inventory.core.repository.EventRepository;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.EventNode;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.EventMapper;

@Repository
public class EventRepositoryImpl implements EventRepository {

    private final EventNeo4jRepository neo4jRepository;
    private final EventMapper mapper;

    public EventRepositoryImpl(EventNeo4jRepository neo4jRepository, EventMapper mapper) {
        this.neo4jRepository = neo4jRepository;
        this.mapper = mapper;
    }

    /** O no e a relacao com o item entram na mesma transacao: historico orfao nao serve para nada. */
    @Override
    @Transactional
    public Event save(Event event) {
        EventNode saved = neo4jRepository.save(mapper.toNode(event));
        neo4jRepository.attachToItem(event.getUnitId(), event.getItemId(), saved.getId());
        return mapper.toDomain(saved);
    }

    @Override
    public PageResult<Event> findPageByItem(UUID unitId, UUID itemId, Pagination pagination) {
        long total = neo4jRepository.countByItem(unitId, itemId);
        if (total == 0) {
            return new PageResult<>(List.of(), pagination.page(), pagination.size(), 0);
        }
        List<EventNode> nodes = neo4jRepository.findPageByItem(unitId, itemId,
                (long) pagination.page() * pagination.size(), pagination.size());
        return new PageResult<>(nodes.stream().map(mapper::toDomain).toList(), pagination.page(),
                pagination.size(), total);
    }

    @Override
    public Optional<Event> findLastByItemAndType(UUID unitId, UUID itemId, EventType type) {
        return neo4jRepository.findLastByItemAndType(unitId, itemId, type.name()).map(mapper::toDomain);
    }

    @Override
    public List<Event> findAllByItem(UUID unitId, UUID itemId) {
        return neo4jRepository.findAllByItem(unitId, itemId).stream().map(mapper::toDomain).toList();
    }
}
