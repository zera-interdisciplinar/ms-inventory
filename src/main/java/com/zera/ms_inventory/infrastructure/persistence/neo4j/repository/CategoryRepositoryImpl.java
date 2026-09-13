package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Repository;

import com.zera.ms_inventory.core.domain.entity.Category;
import com.zera.ms_inventory.core.repository.CategoryRepository;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.CategoryNode;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.CategoryMapper;

@Repository
public class CategoryRepositoryImpl implements CategoryRepository {

    private static final Logger log = LoggerFactory.getLogger(CategoryRepositoryImpl.class);

    private final CategoryNeo4jRepository neo4jRepository;
    private final CategoryMapper mapper;
    private final EmbeddingModel embeddingModel;

    public CategoryRepositoryImpl(CategoryNeo4jRepository neo4jRepository, CategoryMapper mapper,
                                   EmbeddingModel embeddingModel) {
        this.neo4jRepository = neo4jRepository;
        this.mapper = mapper;
        this.embeddingModel = embeddingModel;
    }

    @Override
    public Category save(Category category) {
        CategoryNode node = mapper.toNode(category);
        CategoryNode existing = neo4jRepository
                .findByIdAndUnitId(category.getId(), category.getUnitId())
                .orElse(null);

        String text = category.toEmbeddableText();
        if (existing != null && text.equals(existing.getEmbeddedText())) {
            node.setEmbedding(existing.getEmbedding());
            node.setEmbeddedText(existing.getEmbeddedText());
        } else if (text.isBlank()) {
            node.setEmbedding(null);
            node.setEmbeddedText(null);
        } else {
            try {
                node.setEmbedding(embeddingModel.embed(text));
                node.setEmbeddedText(text);
            } catch (RuntimeException e) {
                log.warn("Embedding provider failed, saving category {} without embedding", category.getId(), e);
                node.setEmbedding(null);
                node.setEmbeddedText(null);
            }
        }

        return mapper.toDomain(neo4jRepository.save(node));
    }

    @Override
    public Optional<Category> findById(UUID unitId, UUID id) {
        return neo4jRepository.findByIdAndUnitId(id, unitId).map(mapper::toDomain);
    }

    @Override
    public List<Category> findAll(UUID unitId) {
        return neo4jRepository.findAllByUnitId(unitId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(UUID unitId, UUID id) {
        neo4jRepository.deleteByIdAndUnitId(id, unitId);
    }
}
