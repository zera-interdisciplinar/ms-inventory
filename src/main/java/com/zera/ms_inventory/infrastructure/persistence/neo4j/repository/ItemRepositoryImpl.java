package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
import com.zera.ms_inventory.core.domain.exception.ModelNotFoundException;
import com.zera.ms_inventory.core.repository.ItemRepository;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.ItemNode;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.ItemMapper;

@Repository
public class ItemRepositoryImpl implements ItemRepository {

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
    public PageResult<Item> findPage(UUID unitId, Pagination pagination) {
        return toPageResult(neo4jRepository.findAllByUnitId(unitId, newestFirst(pagination)), pagination);
    }

    @Override
    public PageResult<Item> findPageByModel(UUID unitId, UUID modelId, Pagination pagination) {
        return toPageResult(neo4jRepository.findAllByUnitIdAndModelId(unitId, modelId, newestFirst(pagination)),
                pagination);
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
