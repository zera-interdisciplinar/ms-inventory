package com.zera.ms_inventory.infrastructure.http.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zera.ms_inventory.core.domain.valueobject.MaterialCode;
import com.zera.ms_inventory.core.usecase.material.FindMaterialByCode;
import com.zera.ms_inventory.core.usecase.material.ListMaterials;
import com.zera.ms_inventory.infrastructure.http.response.MaterialResponse;

/** Catalogo global de materiais (guia "Como descartar"). Nao usa X-Unit-Id. */
@RestController
@RequestMapping("/api/v1/materials")
@PreAuthorize("isAuthenticated()")
public class MaterialController {

    private final ListMaterials listMaterials;
    private final FindMaterialByCode findMaterialByCode;

    public MaterialController(ListMaterials listMaterials, FindMaterialByCode findMaterialByCode) {
        this.listMaterials = listMaterials;
        this.findMaterialByCode = findMaterialByCode;
    }

    @GetMapping
    public ResponseEntity<List<MaterialResponse>> findAll() {
        return ResponseEntity.ok(listMaterials.execute().stream().map(MaterialResponse::from).toList());
    }

    @GetMapping("/{code}")
    public ResponseEntity<MaterialResponse> findByCode(@PathVariable MaterialCode code) {
        return ResponseEntity.ok(MaterialResponse.from(findMaterialByCode.execute(code)));
    }
}
