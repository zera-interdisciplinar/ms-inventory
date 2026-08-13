package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.zera.ms_inventory.core.domain.entity.Category;
import com.zera.ms_inventory.core.repository.CategoryRepository;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.CategoryMapper;

@Repository
public class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryNeo4jRepository neo4jRepository;
    private final CategoryMapper mapper;

    public CategoryRepositoryImpl(CategoryNeo4jRepository neo4jRepository, CategoryMapper mapper) {
        this.neo4jRepository = neo4jRepository;
        this.mapper = mapper;
    }

    @Override
    public Category save(Category category) {
        return mapper.toDomain(neo4jRepository.save(mapper.toNode(category)));
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return neo4jRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Category> findAll() {
        return neo4jRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        neo4jRepository.deleteById(id);
    }
}
