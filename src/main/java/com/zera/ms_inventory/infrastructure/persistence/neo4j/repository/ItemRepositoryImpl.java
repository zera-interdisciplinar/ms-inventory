package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.repository.ItemRepository;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.ItemMapper;

@Repository
public class ItemRepositoryImpl implements ItemRepository {

    private final ItemNeo4jRepository neo4jRepository;
    private final ItemMapper mapper;

    public ItemRepositoryImpl(ItemNeo4jRepository neo4jRepository, ItemMapper mapper) {
        this.neo4jRepository = neo4jRepository;
        this.mapper = mapper;
    }

    @Override
    public Item save(Item item) {
        return mapper.toDomain(neo4jRepository.save(mapper.toNode(item)));
    }

    @Override
    public Optional<Item> findById(UUID id) {
        return neo4jRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Item> findAll() {
        return neo4jRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        neo4jRepository.deleteById(id);
    }
}
