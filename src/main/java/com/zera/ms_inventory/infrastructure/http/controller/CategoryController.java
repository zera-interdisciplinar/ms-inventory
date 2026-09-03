package com.zera.ms_inventory.infrastructure.http.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zera.ms_inventory.core.domain.entity.Category;
import com.zera.ms_inventory.core.usecase.category.CreateCategory;
import com.zera.ms_inventory.core.usecase.category.DeleteCategory;
import com.zera.ms_inventory.core.usecase.category.FindAllCategories;
import com.zera.ms_inventory.core.usecase.category.FindCategoryById;
import com.zera.ms_inventory.core.usecase.category.UpdateCategoryDescription;
import com.zera.ms_inventory.core.usecase.category.UpdateCategoryName;
import com.zera.ms_inventory.infrastructure.http.request.CreateCategoryRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateCategoryDescriptionRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateCategoryNameRequest;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CreateCategory createCategory;
    private final FindAllCategories findAllCategories;
    private final FindCategoryById findCategoryById;
    private final UpdateCategoryName updateCategoryName;
    private final UpdateCategoryDescription updateCategoryDescription;
    private final DeleteCategory deleteCategory;

    public CategoryController(CreateCategory createCategory,
                               FindAllCategories findAllCategories,
                               FindCategoryById findCategoryById,
                               UpdateCategoryName updateCategoryName,
                               UpdateCategoryDescription updateCategoryDescription,
                               DeleteCategory deleteCategory) {
        this.createCategory = createCategory;
        this.findAllCategories = findAllCategories;
        this.findCategoryById = findCategoryById;
        this.updateCategoryName = updateCategoryName;
        this.updateCategoryDescription = updateCategoryDescription;
        this.deleteCategory = deleteCategory;
    }

    @PostMapping
    public ResponseEntity<Category> create(@RequestHeader("X-Unit-Id") UUID unitId,
                                            @RequestBody @Valid CreateCategoryRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Category created = createCategory.execute(unitId, request.name(), request.description(), now, now);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Category>> findAll(@RequestHeader("X-Unit-Id") UUID unitId) {
        return ResponseEntity.ok(findAllCategories.execute(unitId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> findById(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id) {
        return ResponseEntity.ok(findCategoryById.execute(unitId, id));
    }

    @PatchMapping("/{id}/name")
    public ResponseEntity<Category> rename(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id, @RequestBody @Valid UpdateCategoryNameRequest request) {
        return ResponseEntity.ok(updateCategoryName.execute(unitId, id, request.name()));
    }

    @PatchMapping("/{id}/description")
    public ResponseEntity<Category> updateDescription(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id, @RequestBody @Valid UpdateCategoryDescriptionRequest request) {
        return ResponseEntity.ok(updateCategoryDescription.execute(unitId, id, request.description()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestHeader("X-Unit-Id") UUID unitId, @PathVariable UUID id) {
        deleteCategory.execute(unitId, id);
        return ResponseEntity.noContent().build();
    }
}
