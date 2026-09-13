package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.zera.ms_inventory.core.domain.entity.Material;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
import com.zera.ms_inventory.core.domain.exception.CategoryNotFoundException;
import com.zera.ms_inventory.core.domain.exception.MaterialNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.MaterialCode;
import com.zera.ms_inventory.core.repository.ModelRepository;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.MaterialNode;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.ModelNode;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.ModelMapper;

@Repository
public class ModelRepositoryImpl implements ModelRepository {

    private static final int OVER_FETCH_FACTOR = 10;
    private static final Logger log = LoggerFactory.getLogger(ModelRepositoryImpl.class);

    private final ModelNeo4jRepository neo4jRepository;
    private final CategoryNeo4jRepository categoryNeo4jRepository;
    private final MaterialNeo4jRepository materialNeo4jRepository;
    private final ModelMapper mapper;
    private final EmbeddingModel embeddingModel;

    public ModelRepositoryImpl(ModelNeo4jRepository neo4jRepository,
                                CategoryNeo4jRepository categoryNeo4jRepository,
                                MaterialNeo4jRepository materialNeo4jRepository,
                                ModelMapper mapper,
                                EmbeddingModel embeddingModel) {
        this.neo4jRepository = neo4jRepository;
        this.categoryNeo4jRepository = categoryNeo4jRepository;
        this.materialNeo4jRepository = materialNeo4jRepository;
        this.mapper = mapper;
        this.embeddingModel = embeddingModel;
    }

    @Override
    @Transactional
    public Model save(Model model) {
        ModelNode node = mapper.toNode(model);

        if (model.getCategory() != null) {
            UUID categoryId = model.getCategory().getId();
            node.setCategory(categoryNeo4jRepository.findByIdAndUnitId(categoryId, model.getUnitId())
                    .orElseThrow(() -> new CategoryNotFoundException(categoryId)));
        }

        node.setMaterials(storedMaterials(model.getMaterials()));

        ModelNode existing = neo4jRepository
                .findByIdAndUnitId(model.getId(), model.getUnitId())
                .orElse(null);

        if (existing != null) {
            neo4jRepository.removeMaterialsNotIn(model.getId(), model.getUnitId(),
                    model.getMaterials().stream().map(material -> material.getCode().name()).toList());
        }

        String text = model.toEmbeddableText();
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
                log.warn("Embedding provider failed, saving model {} without embedding", model.getId(), e);
                node.setEmbedding(null);
                node.setEmbeddedText(null);
            }
        }

        return mapper.toDomain(neo4jRepository.save(node));
    }

    private Set<MaterialNode> storedMaterials(Set<Material> materials) {
        if (materials.isEmpty()) {
            return new HashSet<>();
        }
        Set<MaterialCode> codes = new HashSet<>();
        materials.forEach(material -> codes.add(material.getCode()));
        Set<MaterialNode> stored = new HashSet<>(materialNeo4jRepository.findAllByCodeIn(codes));
        stored.forEach(node -> codes.remove(node.getCode()));
        if (!codes.isEmpty()) {
            throw new MaterialNotFoundException(codes.iterator().next());
        }
        return stored;
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
    public PageResult<Model> findPage(UUID unitId, Pagination pagination) {
        // mais recentes primeiro, como a lista do app
        Page<ModelNode> page = neo4jRepository.findAllByUnitId(unitId,
                PageRequest.of(pagination.page(), pagination.size(), Sort.by(Sort.Direction.DESC, "createdAt")));
        return new PageResult<>(page.getContent().stream().map(mapper::toDomain).toList(),
                pagination.page(), pagination.size(), page.getTotalElements());
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
