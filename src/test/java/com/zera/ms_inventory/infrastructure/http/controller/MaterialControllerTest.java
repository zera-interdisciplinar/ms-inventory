package com.zera.ms_inventory.infrastructure.http.controller;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.zera.ms_inventory.core.domain.entity.Material;
import com.zera.ms_inventory.core.domain.exception.MaterialNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.MaterialCode;
import com.zera.ms_inventory.core.usecase.material.FindMaterialByCode;
import com.zera.ms_inventory.core.usecase.material.ListMaterials;
import com.zera.ms_inventory.infrastructure.http.handler.GlobalExceptionHandler;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MaterialController.class)
@Import(GlobalExceptionHandler.class)
class MaterialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private ListMaterials listMaterials;
    @MockitoBean private FindMaterialByCode findMaterialByCode;

    private final Material battery = new Material(UUID.randomUUID(), MaterialCode.BATTERY, "Pilhas e baterias",
            true, true, "Leve a pontos de coleta de pilhas e baterias.");

    @Test
    @DisplayName("GET /api/v1/materials - deve listar o catalogo com o guia de descarte")
    void shouldListMaterials() throws Exception {
        when(listMaterials.execute()).thenReturn(List.of(battery));

        mockMvc.perform(get("/api/v1/materials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("BATTERY"))
                .andExpect(jsonPath("$[0].hazardous").value(true))
                .andExpect(jsonPath("$[0].disposalGuide").value("Leve a pontos de coleta de pilhas e baterias."));
    }

    @Test
    @DisplayName("GET /api/v1/materials/{code} - deve retornar o material")
    void shouldFindMaterialByCode() throws Exception {
        when(findMaterialByCode.execute(MaterialCode.BATTERY)).thenReturn(battery);

        mockMvc.perform(get("/api/v1/materials/BATTERY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pilhas e baterias"));
    }

    @Test
    @DisplayName("GET /api/v1/materials/{code} - deve retornar 404 quando o material nao estiver no catalogo")
    void shouldReturn404WhenMaterialIsMissing() throws Exception {
        when(findMaterialByCode.execute(MaterialCode.GLASS)).thenThrow(new MaterialNotFoundException(MaterialCode.GLASS));

        mockMvc.perform(get("/api/v1/materials/GLASS"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/materials/{code} - deve retornar 400 para codigo desconhecido")
    void shouldReturn400ForUnknownCode() throws Exception {
        mockMvc.perform(get("/api/v1/materials/WOOD"))
                .andExpect(status().isBadRequest());
    }
}
