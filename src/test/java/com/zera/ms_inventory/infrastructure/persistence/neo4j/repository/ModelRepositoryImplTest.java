package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.ai.embedding.EmbeddingModel;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
import com.zera.ms_inventory.core.domain.entity.Material;
import com.zera.ms_inventory.core.domain.exception.CategoryNotFoundException;
import com.zera.ms_inventory.core.domain.exception.MaterialNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.ApprovalStatus;
import com.zera.ms_inventory.core.domain.valueobject.MaterialCode;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.MaterialNode;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.ModelNode;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.CategoryMapper;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.ModelMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModelRepositoryImplTest {

    private static final float[] VECTOR = {0.1f, 0.2f, 0.3f};

    @Mock
    private ModelNeo4jRepository neo4jRepository;

    @Mock
    private CategoryNeo4jRepository categoryNeo4jRepository;

    @Mock
    private MaterialNeo4jRepository materialNeo4jRepository;

    @Mock
    private EmbeddingModel embeddingModel;

    private final CategoryMapper categoryMapper = new CategoryMapper();
    private final ModelMapper mapper = new ModelMapper(categoryMapper);

    private ModelRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new ModelRepositoryImpl(neo4jRepository, categoryNeo4jRepository, materialNeo4jRepository, mapper,
                embeddingModel);
    }

    @Test
    void shouldAttachTheStoredCategoryAndEmbedOnFirstSave() {
        Model model = Fixtures.model(Fixtures.UNIT);
        UUID categoryId = model.getCategory().getId();
        when(categoryNeo4jRepository.findByIdAndUnitId(categoryId, Fixtures.UNIT))
                .thenReturn(Optional.of(categoryMapper.toNode(model.getCategory())));
        when(neo4jRepository.findByIdAndUnitId(model.getId(), Fixtures.UNIT)).thenReturn(Optional.empty());
        when(embeddingModel.embed(model.toEmbeddableText())).thenReturn(VECTOR);
        when(neo4jRepository.save(any(ModelNode.class))).thenAnswer(i -> i.getArgument(0));

        Model result = repository.save(model);

        assertEquals(categoryId, result.getCategory().getId());
        verify(neo4jRepository).save(org.mockito.ArgumentMatchers.argThat(node -> {
            assertEquals(model.toEmbeddableText(), node.getEmbeddedText());
            assertEquals(categoryId, node.getCategory().getId());
            return true;
        }));
    }

    @Test
    void shouldRejectCategoryFromAnotherUnit() {
        Model model = Fixtures.model(Fixtures.UNIT);
        when(categoryNeo4jRepository.findByIdAndUnitId(model.getCategory().getId(), Fixtures.UNIT))
                .thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> repository.save(model));
        verify(neo4jRepository, never()).save(any(ModelNode.class));
    }

    @Test
    void shouldReuseTheStoredVectorWhenTheTextDidNotChange() {
        Model model = Fixtures.model(Fixtures.UNIT);
        ModelNode stored = mapper.toNode(model);
        stored.setEmbedding(VECTOR);
        stored.setEmbeddedText(model.toEmbeddableText());
        when(categoryNeo4jRepository.findByIdAndUnitId(model.getCategory().getId(), Fixtures.UNIT))
                .thenReturn(Optional.of(categoryMapper.toNode(model.getCategory())));
        when(neo4jRepository.findByIdAndUnitId(model.getId(), Fixtures.UNIT)).thenReturn(Optional.of(stored));
        when(neo4jRepository.save(any(ModelNode.class))).thenAnswer(i -> i.getArgument(0));

        repository.save(model);

        verify(embeddingModel, never()).embed(anyString());
    }

    @Test
    void shouldSaveModelWithoutCategory() {
        Model model = new Model(UUID.randomUUID(), Fixtures.UNIT, "Laptop", "Acme", 24, 60, java.util.Set.of(), null, null, null);
        when(neo4jRepository.findByIdAndUnitId(model.getId(), Fixtures.UNIT)).thenReturn(Optional.empty());
        when(embeddingModel.embed(model.toEmbeddableText())).thenReturn(VECTOR);
        when(neo4jRepository.save(any(ModelNode.class))).thenAnswer(i -> i.getArgument(0));

        assertEquals(model.getId(), repository.save(model).getId());
        verify(categoryNeo4jRepository, never()).findByIdAndUnitId(any(), any());
    }

    @Test
    void shouldFindByIdWithinTheUnit() {
        UUID id = UUID.randomUUID();
        when(neo4jRepository.findByIdAndUnitId(id, Fixtures.UNIT))
                .thenReturn(Optional.of(mapper.toNode(Fixtures.model(id, Fixtures.UNIT))));

        assertEquals(id, repository.findById(Fixtures.UNIT, id).orElseThrow().getId());
    }

    @Test
    void shouldReturnEmptyForAnotherUnit() {
        UUID id = UUID.randomUUID();
        when(neo4jRepository.findByIdAndUnitId(id, Fixtures.OTHER_UNIT)).thenReturn(Optional.empty());

        assertTrue(repository.findById(Fixtures.OTHER_UNIT, id).isEmpty());
    }

    @Test
    void shouldFindAllWithinTheUnit() {
        when(neo4jRepository.findAllByUnitId(Fixtures.UNIT))
                .thenReturn(List.of(mapper.toNode(Fixtures.model(Fixtures.UNIT))));

        assertEquals(1, repository.findAll(Fixtures.UNIT).size());
    }

    @Test
    void shouldOverFetchTenTimesTheLimitAndScopeTheSearchToTheUnit() {
        when(embeddingModel.embed("bateria de litio")).thenReturn(VECTOR);
        when(neo4jRepository.semanticSearch(any(), eq(Fixtures.UNIT), anyInt(), anyInt()))
                .thenReturn(List.of(mapper.toNode(Fixtures.model(Fixtures.UNIT))));

        List<Model> result = repository.semanticSearch(Fixtures.UNIT, "bateria de litio", 5);

        assertEquals(1, result.size());
        verify(neo4jRepository).semanticSearch(List.of(0.1f, 0.2f, 0.3f), Fixtures.UNIT, 50, 5);
    }

    @Test
    void shouldDeleteWithinTheUnit() {
        UUID id = UUID.randomUUID();

        repository.deleteById(Fixtures.UNIT, id);

        verify(neo4jRepository).deleteByIdAndUnitId(id, Fixtures.UNIT);
    }

    @Test
    void shouldPageNewestFirstWithinTheUnit() {
        PageRequest request = PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        when(neo4jRepository.findAllByUnitId(Fixtures.UNIT, request))
                .thenReturn(new PageImpl<>(List.of(mapper.toNode(Fixtures.model(Fixtures.UNIT))), request, 11));

        PageResult<Model> result = repository.findPage(Fixtures.UNIT, null, new Pagination(1, 10));

        assertEquals(1, result.content().size());
        assertEquals(11, result.totalElements());
        assertEquals(2, result.totalPages());
    }

    private Model modelMadeOf(MaterialCode... codes) {
        java.util.Set<Material> materials = new java.util.HashSet<>();
        for (MaterialCode code : codes) {
            materials.add(new Material(UUID.randomUUID(), code, code.name(), true, code == MaterialCode.BATTERY, "guia"));
        }
        return new Model(UUID.randomUUID(), Fixtures.UNIT, "Laptop", "Acme", 24, 60, materials,
                2.5, "Com carregador", null);
    }

    @Test
    void shouldAttachTheStoredCatalogMaterialsOnSave() {
        Model model = modelMadeOf(MaterialCode.BATTERY, MaterialCode.PLASTIC);
        MaterialNode battery = new MaterialNode(UUID.randomUUID(), MaterialCode.BATTERY, "Pilhas e baterias", true, true, "g");
        MaterialNode plastic = new MaterialNode(UUID.randomUUID(), MaterialCode.PLASTIC, "Plástico", true, false, "g");
        when(materialNeo4jRepository.findAllByCodeIn(java.util.Set.of(MaterialCode.BATTERY, MaterialCode.PLASTIC)))
                .thenReturn(List.of(battery, plastic));
        when(neo4jRepository.findByIdAndUnitId(model.getId(), Fixtures.UNIT)).thenReturn(Optional.empty());
        when(embeddingModel.embed(anyString())).thenReturn(VECTOR);
        when(neo4jRepository.save(any(ModelNode.class))).thenAnswer(i -> i.getArgument(0));

        Model result = repository.save(model);

        assertEquals(2, result.getMaterials().size());
        assertTrue(result.isHazardous());
        assertEquals(2.5, result.getEstimatedWeightKg());
        assertEquals("Com carregador", result.getNotes());
    }

    @Test
    void shouldRejectMaterialsMissingFromTheCatalog() {
        Model model = modelMadeOf(MaterialCode.GLASS);
        when(materialNeo4jRepository.findAllByCodeIn(java.util.Set.of(MaterialCode.GLASS))).thenReturn(List.of());

        assertThrows(MaterialNotFoundException.class, () -> repository.save(model));
        verify(neo4jRepository, never()).save(any(ModelNode.class));
    }

    @Test
    void shouldDropMaterialsNoLongerInTheModelWhenUpdating() {
        Model model = modelMadeOf(MaterialCode.PLASTIC);
        MaterialNode plastic = new MaterialNode(UUID.randomUUID(), MaterialCode.PLASTIC, "Plástico", true, false, "g");
        when(materialNeo4jRepository.findAllByCodeIn(java.util.Set.of(MaterialCode.PLASTIC))).thenReturn(List.of(plastic));
        when(neo4jRepository.findByIdAndUnitId(model.getId(), Fixtures.UNIT)).thenReturn(Optional.of(mapper.toNode(model)));
        when(embeddingModel.embed(anyString())).thenReturn(VECTOR);
        when(neo4jRepository.save(any(ModelNode.class))).thenAnswer(i -> i.getArgument(0));

        repository.save(model);

        verify(neo4jRepository).removeMaterialsNotIn(model.getId(), Fixtures.UNIT, List.of("PLASTIC"));
    }

    @Test
    void shouldNotTouchRelationshipsOnFirstSave() {
        Model model = modelMadeOf(MaterialCode.PLASTIC);
        MaterialNode plastic = new MaterialNode(UUID.randomUUID(), MaterialCode.PLASTIC, "Plástico", true, false, "g");
        when(materialNeo4jRepository.findAllByCodeIn(java.util.Set.of(MaterialCode.PLASTIC))).thenReturn(List.of(plastic));
        when(neo4jRepository.findByIdAndUnitId(model.getId(), Fixtures.UNIT)).thenReturn(Optional.empty());
        when(embeddingModel.embed(anyString())).thenReturn(VECTOR);
        when(neo4jRepository.save(any(ModelNode.class))).thenAnswer(i -> i.getArgument(0));

        repository.save(model);

        verify(neo4jRepository, never()).removeMaterialsNotIn(any(), any(), any());
    }

    @Test
    void shouldFilterThePageByApprovalStatus() {
        PageRequest request = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        when(neo4jRepository.findAllByUnitIdAndApprovalStatus(Fixtures.UNIT, ApprovalStatus.PENDING, request))
                .thenReturn(new PageImpl<>(List.of(mapper.toNode(Fixtures.model(Fixtures.UNIT))), request, 1));

        PageResult<Model> result = repository.findPage(Fixtures.UNIT, ApprovalStatus.PENDING, new Pagination(0, 20));

        assertEquals(1, result.totalElements());
        verify(neo4jRepository, never()).findAllByUnitId(any(UUID.class), any(PageRequest.class));
    }
}
