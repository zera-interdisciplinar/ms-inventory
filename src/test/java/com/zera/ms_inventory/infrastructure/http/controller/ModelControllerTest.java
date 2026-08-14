package com.zera.ms_inventory.infrastructure.http.controller;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.exception.ModelNotFoundException;
import com.zera.ms_inventory.core.usecase.model.CreateModel;
import com.zera.ms_inventory.core.usecase.model.DeleteModel;
import com.zera.ms_inventory.core.usecase.model.FindAllModels;
import com.zera.ms_inventory.core.usecase.model.FindModelById;
import com.zera.ms_inventory.core.usecase.model.UpdateModelExpectedLifespanMonths;
import com.zera.ms_inventory.core.usecase.model.UpdateModelHazardousMaterials;
import com.zera.ms_inventory.core.usecase.model.UpdateModelManufacturer;
import com.zera.ms_inventory.core.usecase.model.UpdateModelName;
import com.zera.ms_inventory.core.usecase.model.UpdateModelWarrantyMonths;
import com.zera.ms_inventory.infrastructure.http.handler.GlobalExceptionHandler;
import com.zera.ms_inventory.infrastructure.http.request.CreateModelRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateModelExpectedLifespanMonthsRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateModelHazardousMaterialsRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateModelManufacturerRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateModelNameRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateModelWarrantyMonthsRequest;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ModelController.class)
@org.springframework.context.annotation.Import(GlobalExceptionHandler.class)
class ModelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private CreateModel createModel;
    @MockitoBean private FindAllModels findAllModels;
    @MockitoBean private FindModelById findModelById;
    @MockitoBean private UpdateModelName updateModelName;
    @MockitoBean private UpdateModelManufacturer updateModelManufacturer;
    @MockitoBean private UpdateModelWarrantyMonths updateModelWarrantyMonths;
    @MockitoBean private UpdateModelExpectedLifespanMonths updateModelExpectedLifespanMonths;
    @MockitoBean private UpdateModelHazardousMaterials updateModelHazardousMaterials;
    @MockitoBean private DeleteModel deleteModel;

    @Test
    @DisplayName("POST /api/v1/models - deve criar model e retornar 201")
    void shouldCreateModel() throws Exception {
        UUID id = UUID.randomUUID();
        Model model = new Model(id, "Laptop X1", "Acme", 24, 60, Set.of("Lithium"));
        when(createModel.execute(eq("Laptop X1"), eq("Acme"), eq(24), eq(60), eq(Set.of("Lithium"))))
                .thenReturn(model);

        mockMvc.perform(post("/api/v1/models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateModelRequest("Laptop X1", "Acme", 24, 60, Set.of("Lithium")))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Laptop X1"));
    }

    @Test
    @DisplayName("POST /api/v1/models - deve retornar 400 quando o nome estiver em branco")
    void shouldReturn400WhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateModelRequest("", "Acme", 24, 60, Set.of("Lithium")))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/models - deve retornar 400 quando warrantyMonths não for positivo")
    void shouldReturn400WhenWarrantyMonthsIsNotPositive() throws Exception {
        mockMvc.perform(post("/api/v1/models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateModelRequest("Laptop X1", "Acme", 0, 60, Set.of("Lithium")))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/models - deve listar todos os models")
    void shouldFindAllModels() throws Exception {
        Model model = new Model(UUID.randomUUID(), "Laptop X1", "Acme", 24, 60, Set.of("Lithium"));
        when(findAllModels.execute()).thenReturn(List.of(model));

        mockMvc.perform(get("/api/v1/models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Laptop X1"));
    }

    @Test
    @DisplayName("GET /api/v1/models/{id} - deve retornar o model")
    void shouldFindModelById() throws Exception {
        UUID id = UUID.randomUUID();
        Model model = new Model(id, "Laptop X1", "Acme", 24, 60, Set.of("Lithium"));
        when(findModelById.execute(id)).thenReturn(model);

        mockMvc.perform(get("/api/v1/models/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/models/{id} - deve retornar 404 quando o model não existir")
    void shouldReturn404WhenModelDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        when(findModelById.execute(id)).thenThrow(new ModelNotFoundException(id));

        mockMvc.perform(get("/api/v1/models/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/v1/models/{id}/name - deve renomear o model")
    void shouldRenameModel() throws Exception {
        UUID id = UUID.randomUUID();
        Model model = new Model(id, "Laptop X2", "Acme", 24, 60, Set.of("Lithium"));
        when(updateModelName.execute(id, "Laptop X2")).thenReturn(model);

        mockMvc.perform(patch("/api/v1/models/{id}/name", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateModelNameRequest("Laptop X2"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop X2"));
    }

    @Test
    @DisplayName("PATCH /api/v1/models/{id}/manufacturer - deve atualizar o fabricante")
    void shouldUpdateManufacturer() throws Exception {
        UUID id = UUID.randomUUID();
        Model model = new Model(id, "Laptop X1", "Globex", 24, 60, Set.of("Lithium"));
        when(updateModelManufacturer.execute(id, "Globex")).thenReturn(model);

        mockMvc.perform(patch("/api/v1/models/{id}/manufacturer", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateModelManufacturerRequest("Globex"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manufacturer").value("Globex"));
    }

    @Test
    @DisplayName("PATCH /api/v1/models/{id}/warranty-months - deve atualizar a garantia")
    void shouldUpdateWarrantyMonths() throws Exception {
        UUID id = UUID.randomUUID();
        Model model = new Model(id, "Laptop X1", "Acme", 36, 60, Set.of("Lithium"));
        when(updateModelWarrantyMonths.execute(id, 36)).thenReturn(model);

        mockMvc.perform(patch("/api/v1/models/{id}/warranty-months", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateModelWarrantyMonthsRequest(36))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.warrantyMonths").value(36));
    }

    @Test
    @DisplayName("PATCH /api/v1/models/{id}/expected-lifespan-months - deve atualizar a vida útil esperada")
    void shouldUpdateExpectedLifespanMonths() throws Exception {
        UUID id = UUID.randomUUID();
        Model model = new Model(id, "Laptop X1", "Acme", 24, 72, Set.of("Lithium"));
        when(updateModelExpectedLifespanMonths.execute(id, 72)).thenReturn(model);

        mockMvc.perform(patch("/api/v1/models/{id}/expected-lifespan-months", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateModelExpectedLifespanMonthsRequest(72))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expectedLifespanMonths").value(72));
    }

    @Test
    @DisplayName("PATCH /api/v1/models/{id}/hazardous-materials - deve atualizar os materiais perigosos")
    void shouldUpdateHazardousMaterials() throws Exception {
        UUID id = UUID.randomUUID();
        Model model = new Model(id, "Laptop X1", "Acme", 24, 60, Set.of("Mercury"));
        when(updateModelHazardousMaterials.execute(id, Set.of("Mercury"))).thenReturn(model);

        mockMvc.perform(patch("/api/v1/models/{id}/hazardous-materials", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateModelHazardousMaterialsRequest(Set.of("Mercury")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hazardousMaterials[0]").value("Mercury"));
    }

    @Test
    @DisplayName("DELETE /api/v1/models/{id} - deve remover o model e retornar 204")
    void shouldDeleteModel() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/models/{id}", id))
                .andExpect(status().isNoContent());

        verify(deleteModel).execute(id);
    }
}
