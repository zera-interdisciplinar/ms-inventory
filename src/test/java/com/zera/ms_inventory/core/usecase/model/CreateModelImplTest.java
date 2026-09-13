package com.zera.ms_inventory.core.usecase.model;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Material;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.exception.CategoryNotFoundException;
import com.zera.ms_inventory.core.domain.exception.MaterialNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.ActorRole;
import com.zera.ms_inventory.core.domain.valueobject.ApprovalStatus;
import com.zera.ms_inventory.core.domain.valueobject.MaterialCode;
import com.zera.ms_inventory.core.repository.CategoryRepository;
import com.zera.ms_inventory.core.repository.ModelRepository;
import com.zera.ms_inventory.core.usecase.material.MaterialResolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateModelImplTest {

    @Mock
    private ModelRepository modelRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MaterialResolver materialResolver;

    private final Material battery = new Material(UUID.randomUUID(), MaterialCode.BATTERY, "Pilhas e baterias",
            true, true, "guia");

    private static final UUID OPERATOR = UUID.randomUUID();

    private CreateModelCommand command(UUID categoryId) {
        return command(categoryId, new Actor(OPERATOR, ActorRole.EMPLOYEE));
    }

    private CreateModelCommand command(UUID categoryId, Actor actor) {
        return new CreateModelCommand(Fixtures.UNIT, "Laptop X1", "Acme", null, null, Set.of(MaterialCode.BATTERY),
                2.3, "Com carregador", categoryId, actor);
    }

    @Test
    void shouldCreateModelWithCatalogMaterialsWeightAndNotes() {
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findById(Fixtures.UNIT, categoryId))
                .thenReturn(Optional.of(Fixtures.category(categoryId, Fixtures.UNIT)));
        when(materialResolver.resolve(Set.of(MaterialCode.BATTERY))).thenReturn(Set.of(battery));
        when(modelRepository.save(any(Model.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateModelImpl useCase = new CreateModelImpl(modelRepository, categoryRepository, materialResolver);
        Model result = useCase.execute(command(categoryId));

        assertNotNull(result.getId());
        assertEquals(Fixtures.UNIT, result.getUnitId());
        assertEquals("Laptop X1", result.getName());
        assertEquals(categoryId, result.getCategory().getId());
        assertEquals(Set.of(battery), result.getMaterials());
        assertTrue(result.isHazardous());
        assertEquals(2.3, result.getEstimatedWeightKg());
        assertEquals("Com carregador", result.getNotes());
        assertEquals(ApprovalStatus.PENDING, result.getApprovalStatus());
        assertEquals(OPERATOR, result.getCreatedBy());
        verify(modelRepository).save(result);
    }

    @Test
    void shouldRejectCategoryFromAnotherUnit() {
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findById(Fixtures.UNIT, categoryId)).thenReturn(Optional.empty());

        CreateModelImpl useCase = new CreateModelImpl(modelRepository, categoryRepository, materialResolver);

        assertThrows(CategoryNotFoundException.class, () -> useCase.execute(command(categoryId)));
        verify(modelRepository, never()).save(any(Model.class));
    }

    @Test
    void shouldNotSaveWhenAMaterialIsMissingFromTheCatalog() {
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findById(Fixtures.UNIT, categoryId))
                .thenReturn(Optional.of(Fixtures.category(categoryId, Fixtures.UNIT)));
        when(materialResolver.resolve(Set.of(MaterialCode.BATTERY)))
                .thenThrow(new MaterialNotFoundException(MaterialCode.BATTERY));

        CreateModelImpl useCase = new CreateModelImpl(modelRepository, categoryRepository, materialResolver);

        assertThrows(MaterialNotFoundException.class, () -> useCase.execute(command(categoryId)));
        verify(modelRepository, never()).save(any(Model.class));
    }

    @Test
    void shouldApproveModelsRegisteredByAManager() {
        UUID categoryId = UUID.randomUUID();
        UUID manager = UUID.randomUUID();
        when(categoryRepository.findById(Fixtures.UNIT, categoryId))
                .thenReturn(Optional.of(Fixtures.category(categoryId, Fixtures.UNIT)));
        when(materialResolver.resolve(Set.of(MaterialCode.BATTERY))).thenReturn(Set.of(battery));
        when(modelRepository.save(any(Model.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Model result = new CreateModelImpl(modelRepository, categoryRepository, materialResolver)
                .execute(command(categoryId, new Actor(manager, ActorRole.MANAGER)));

        assertEquals(ApprovalStatus.APPROVED, result.getApprovalStatus());
        assertEquals(manager, result.getCreatedBy());
        assertEquals(manager, result.getReviewedBy());
        assertNotNull(result.getReviewedAt());
    }
}
