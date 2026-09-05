package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Repository;

import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.exception.CategoryNotFoundException;
import com.zera.ms_inventory.core.repository.ModelRepository;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.ModelNode;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.ModelMapper;

@Repository
public class ModelRepositoryImpl implements ModelRepository {

    private static final int OVER_FETCH_FACTOR = 10;

    private final ModelNeo4jRepository neo4jRepository;
    private final CategoryNeo4jRepository categoryNeo4jRepository;
    private final ModelMapper mapper;
    private final EmbeddingModel embeddingModel;

    public ModelRepositoryImpl(ModelNeo4jRepository neo4jRepository,
                                CategoryNeo4jRepository categoryNeo4jRepository,
                                ModelMapper mapper,
                                EmbeddingModel embeddingModel) {
        this.neo4jRepository = neo4jRepository;
        this.categoryNeo4jRepository = categoryNeo4jRepository;
        this.mapper = mapper;
        this.embeddingModel = embeddingModel;
    }

    @Override
    public Model save(Model model) {
        ModelNode node = mapper.toNode(model);

        if (model.getCategory() != null) {
            UUID categoryId = model.getCategory().getId();
            node.setCategory(categoryNeo4jRepository.findByIdAndUnitId(categoryId, model.getUnitId())
                    .orElseThrow(() -> new CategoryNotFoundException(categoryId)));
        }

        ModelNode existing = neo4jRepository
                .findByIdAndUnitId(model.getId(), model.getUnitId())
                .orElse(null);

        String text = model.toEmbeddableText();
        if (existing != null && text.equals(existing.getEmbeddedText())) {
            node.setEmbedding(existing.getEmbedding());
            node.setEmbeddedText(existing.getEmbeddedText());
        } else {
            node.setEmbedding(embeddingModel.embed(text));
            node.setEmbeddedText(text);
        }

        return mapper.toDomain(neo4jRepository.save(node));
    }

    @Override
    public Optional<Model> findById(UUID unitId, UUID id) {
        return neo4jRepository.findByIdAndUnitId(id, unitId).map(mapper::toDomain);
    }

    @Override
    public List<Model> findAll(UUID unitId) {
        return neo4jRepository.findAllByUnitId(unitId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Model> semanticSearch(UUID unitId, String query, int limit) {
        float[] embedded = embeddingModel.embed(query);
        List<Float> queryVector = new ArrayList<>(embedded.length);
        for (float value : embedded) {
            queryVector.add(value);
        }
        return neo4jRepository.semanticSearch(queryVector, unitId, limit * OVER_FETCH_FACTOR, limit)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(UUID unitId, UUID id) {
        neo4jRepository.deleteByIdAndUnitId(id, unitId);
    }
}
