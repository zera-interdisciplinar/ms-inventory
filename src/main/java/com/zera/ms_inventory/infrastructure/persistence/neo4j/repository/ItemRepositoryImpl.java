package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.ItemFilter;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
import com.zera.ms_inventory.core.domain.exception.ModelNotFoundException;
import com.zera.ms_inventory.core.repository.ItemRepository;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.ItemNode;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.ItemMapper;

@Repository
public class ItemRepositoryImpl implements ItemRepository {

    /** Elegivel para descarte e o que a maquina de estados deixa ir para DISPOSED. */
    private static final List<String> DISPOSABLE_STATUSES = java.util.Arrays.stream(ItemStatus.values())
            .filter(status -> status.canTransitionTo(ItemStatus.DISPOSED))
            .map(ItemStatus::name)
            .toList();

    private final ItemNeo4jRepository neo4jRepository;
    private final ModelNeo4jRepository modelNeo4jRepository;
    private final ItemMapper mapper;

    public ItemRepositoryImpl(ItemNeo4jRepository neo4jRepository,
                              ModelNeo4jRepository modelNeo4jRepository,
                              ItemMapper mapper) {
        this.neo4jRepository = neo4jRepository;
        this.modelNeo4jRepository = modelNeo4jRepository;
        this.mapper = mapper;
    }

    @Override
    public Item save(Item item) {
        ItemNode node = mapper.toNode(item);

        if (item.getModel() != null) {
            UUID modelId = item.getModel().getId();
            node.setModel(modelNeo4jRepository.findByIdAndUnitId(modelId, item.getUnitId())
                    .orElseThrow(() -> new ModelNotFoundException(modelId)));
        }

        return mapper.toDomain(neo4jRepository.save(node));
    }

    @Override
    public Optional<Item> findById(UUID unitId, UUID id) {
        return neo4jRepository.findByIdAndUnitId(id, unitId).map(mapper::toDomain);
    }

    @Override
    public List<Item> findAll(UUID unitId) {
        return neo4jRepository.findAllByUnitId(unitId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public PageResult<Item> findPage(UUID unitId, ItemFilter filter, Pagination pagination) {
        String status = filter.status() == null ? null : filter.status().name();
        boolean eligible = filter.onlyEligibleForDisposal();
        long total = neo4jRepository.countFiltered(unitId, status, filter.categoryId(), filter.modelId(),
                filter.query(), eligible, DISPOSABLE_STATUSES);
        if (total == 0) {
            return new PageResult<>(List.of(), pagination.page(), pagination.size(), 0);
        }
        List<ItemNode> nodes = neo4jRepository.findFilteredPage(unitId, status, filter.categoryId(),
                filter.modelId(), filter.query(), eligible, DISPOSABLE_STATUSES,
                (long) pagination.page() * pagination.size(), pagination.size());
        return new PageResult<>(nodes.stream().map(mapper::toDomain).toList(), pagination.page(), pagination.size(),
                total);
    }

    @Override
    public PageResult<Item> findPageByModel(UUID unitId, UUID modelId, Pagination pagination) {
        return toPageResult(neo4jRepository.findAllByUnitIdAndModelId(unitId, modelId, newestFirst(pagination)),
                pagination);
    }

    @Override
    public boolean existsByModel(UUID unitId, UUID modelId) {
        return neo4jRepository.existsByUnitIdAndModelId(unitId, modelId);
    }

    @Override
    public long countByModel(UUID unitId, UUID modelId) {
        return neo4jRepository.countByUnitIdAndModelId(unitId, modelId);
    }

    @Override
    public boolean existsAnyWithId(UUID id) {
        return neo4jRepository.existsById(id);
    }

    @Override
    public boolean existsByDisplayCode(UUID unitId, String displayCode) {
        return neo4jRepository.existsByUnitIdAndDisplayCode(unitId, displayCode);
    }

    @Override
    public Optional<Item> findByBarcode(UUID unitId, String barcode) {
        return neo4jRepository.findByUnitIdAndBarcode(unitId, barcode).map(mapper::toDomain);
    }

    // mais recentes primeiro, como a lista do app
    private static PageRequest newestFirst(Pagination pagination) {
        return PageRequest.of(pagination.page(), pagination.size(), Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private PageResult<Item> toPageResult(Page<ItemNode> page, Pagination pagination) {
        return new PageResult<>(page.getContent().stream().map(mapper::toDomain).toList(),
                pagination.page(), pagination.size(), page.getTotalElements());
    }

    @Override
    public List<Item> findAllByModelIds(UUID unitId, List<UUID> modelIds) {
        if (modelIds.isEmpty()) {
            return List.of();
        }
        return neo4jRepository.findAllByUnitIdAndModelIdIn(unitId, modelIds).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(UUID unitId, UUID id) {
        neo4jRepository.deleteByIdAndUnitId(id, unitId);
    }
}
