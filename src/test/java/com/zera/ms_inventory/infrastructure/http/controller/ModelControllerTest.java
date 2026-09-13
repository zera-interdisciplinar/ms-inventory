package com.zera.ms_inventory.infrastructure.http.controller;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.entity.Material;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.exception.ModelInUseException;
import com.zera.ms_inventory.core.domain.exception.ModelNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.ActorRole;
import com.zera.ms_inventory.core.domain.valueobject.ApprovalStatus;
import com.zera.ms_inventory.core.domain.valueobject.MaterialCode;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
import com.zera.ms_inventory.core.usecase.model.CreateModel;
import com.zera.ms_inventory.core.usecase.model.CreateModelCommand;
import com.zera.ms_inventory.core.usecase.model.DeleteModel;
import com.zera.ms_inventory.core.usecase.model.FindModelById;
import com.zera.ms_inventory.core.usecase.model.ListModelItems;
import com.zera.ms_inventory.core.usecase.model.ListModels;
import com.zera.ms_inventory.core.usecase.model.UpdateModelExpectedLifespanMonths;
import com.zera.ms_inventory.core.usecase.model.UpdateModelMaterials;
import com.zera.ms_inventory.core.usecase.model.UpdateModelManufacturer;
import com.zera.ms_inventory.core.usecase.model.UpdateModelName;
import com.zera.ms_inventory.core.usecase.model.UpdateModelWarrantyMonths;
import com.zera.ms_inventory.infrastructure.http.handler.GlobalExceptionHandler;
import com.zera.ms_inventory.infrastructure.http.response.ItemResponses;
import com.zera.ms_inventory.infrastructure.http.request.CreateModelRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateModelExpectedLifespanMonthsRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateModelMaterialsRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateModelManufacturerRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateModelNameRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateModelWarrantyMonthsRequest;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ModelController.class)
@org.springframework.context.annotation.Import({GlobalExceptionHandler.class, ItemResponses.class})
class ModelControllerTest {


    private static final UUID OPERATOR_ID = UUID.fromString("00000000-0000-0000-0000-0000000000e1");
    private static final UUID UNIT = com.zera.ms_inventory.Fixtures.UNIT;
    private static final UUID CATEGORY_ID = UUID.fromString("00000000-0000-0000-0000-0000000000c3");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private com.zera.ms_inventory.core.repository.PhotoStorage photoStorage;

    @MockitoBean private CreateModel createModel;
    @MockitoBean private ListModels listModels;
    @MockitoBean private ListModelItems listModelItems;
    @MockitoBean private FindModelById findModelById;
    @MockitoBean private UpdateModelName updateModelName;
    @MockitoBean private UpdateModelManufacturer updateModelManufacturer;
    @MockitoBean private UpdateModelWarrantyMonths updateModelWarrantyMonths;
    @MockitoBean private UpdateModelExpectedLifespanMonths updateModelExpectedLifespanMonths;
    @MockitoBean private UpdateModelMaterials updateModelMaterials;
    @MockitoBean private DeleteModel deleteModel;

    @Test
    @DisplayName("POST /api/v1/models - deve criar model e retornar 201")
    void shouldCreateModel() throws Exception {
        UUID id = UUID.randomUUID();
        Model model = new Model(id, UNIT, "Laptop X1", "Acme", 24, 60, Set.of(), null, null, com.zera.ms_inventory.Fixtures.category(CATEGORY_ID, UNIT));
        when(createModel.execute(new CreateModelCommand(UNIT, "Laptop X1", "Acme", 24, 60, Set.of(MaterialCode.BATTERY),
                2.3, "Com carregador", CATEGORY_ID, new Actor(OPERATOR_ID, ActorRole.EMPLOYEE)))).thenReturn(model);
        model.registerBy(new Actor(OPERATOR_ID, ActorRole.EMPLOYEE));

        mockMvc.perform(post("/api/v1/models")
                        .principal(new TestingAuthenticationToken(OPERATOR_ID.toString(), null, "ROLE_EMPLOYEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateModelRequest("Laptop X1", "Acme", 24, 60, Set.of(MaterialCode.BATTERY), 2.3, "Com carregador", CATEGORY_ID)))
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.approvalStatus").value("PENDING"))
                .andExpect(jsonPath("$.createdBy").value(OPERATOR_ID.toString()))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Laptop X1"));
    }

    @Test
    @DisplayName("POST /api/v1/models - deve retornar 400 quando o nome estiver em branco")
    void shouldReturn400WhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateModelRequest("", "Acme", 24, 60, Set.of(MaterialCode.BATTERY), null, null, CATEGORY_ID)))
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/models - deve retornar 400 quando warrantyMonths não for positivo")
    void shouldReturn400WhenWarrantyMonthsIsNotPositive() throws Exception {
        mockMvc.perform(post("/api/v1/models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateModelRequest("Laptop X1", "Acme", 0, 60, Set.of(MaterialCode.BATTERY), null, null, CATEGORY_ID)))
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/models - deve listar todos os models")
    void shouldFindAllModels() throws Exception {
        Model model = new Model(UUID.randomUUID(), UNIT, "Laptop X1", "Acme", 24, 60, Set.of(), null, null, com.zera.ms_inventory.Fixtures.category(CATEGORY_ID, UNIT));
        when(listModels.execute(UNIT, null, new Pagination(0, 20))).thenReturn(new PageResult<>(List.of(model), 0, 20, 1));

        mockMvc.perform(get("/api/v1/models")
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Laptop X1"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/models/{id} - deve retornar o model")
    void shouldFindModelById() throws Exception {
        UUID id = UUID.randomUUID();
        Model model = new Model(id, UNIT, "Laptop X1", "Acme", 24, 60, Set.of(), null, null, com.zera.ms_inventory.Fixtures.category(CATEGORY_ID, UNIT));
        when(findModelById.execute(UNIT, id)).thenReturn(model);

        mockMvc.perform(get("/api/v1/models/{id}", id)
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/models/{id} - deve retornar 404 quando o model não existir")
    void shouldReturn404WhenModelDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        when(findModelById.execute(UNIT, id)).thenThrow(new ModelNotFoundException(id));

        mockMvc.perform(get("/api/v1/models/{id}", id)
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/v1/models/{id}/name - deve renomear o model")
    void shouldRenameModel() throws Exception {
        UUID id = UUID.randomUUID();
        Model model = new Model(id, UNIT, "Laptop X2", "Acme", 24, 60, Set.of(), null, null, com.zera.ms_inventory.Fixtures.category(CATEGORY_ID, UNIT));
        when(updateModelName.execute(UNIT, id, "Laptop X2")).thenReturn(model);

        mockMvc.perform(patch("/api/v1/models/{id}/name", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateModelNameRequest("Laptop X2")))
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop X2"));
    }

    @Test
    @DisplayName("PATCH /api/v1/models/{id}/manufacturer - deve atualizar o fabricante")
    void shouldUpdateManufacturer() throws Exception {
        UUID id = UUID.randomUUID();
        Model model = new Model(id, UNIT, "Laptop X1", "Globex", 24, 60, Set.of(), null, null, com.zera.ms_inventory.Fixtures.category(CATEGORY_ID, UNIT));
        when(updateModelManufacturer.execute(UNIT, id, "Globex")).thenReturn(model);

        mockMvc.perform(patch("/api/v1/models/{id}/manufacturer", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateModelManufacturerRequest("Globex")))
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manufacturer").value("Globex"));
    }

    @Test
    @DisplayName("PATCH /api/v1/models/{id}/warranty-months - deve atualizar a garantia")
    void shouldUpdateWarrantyMonths() throws Exception {
        UUID id = UUID.randomUUID();
        Model model = new Model(id, UNIT, "Laptop X1", "Acme", 36, 60, Set.of(), null, null, com.zera.ms_inventory.Fixtures.category(CATEGORY_ID, UNIT));
        when(updateModelWarrantyMonths.execute(UNIT, id, 36)).thenReturn(model);

        mockMvc.perform(patch("/api/v1/models/{id}/warranty-months", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateModelWarrantyMonthsRequest(36)))
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.warrantyMonths").value(36));
    }

    @Test
    @DisplayName("PATCH /api/v1/models/{id}/expected-lifespan-months - deve atualizar a vida útil esperada")
    void shouldUpdateExpectedLifespanMonths() throws Exception {
        UUID id = UUID.randomUUID();
        Model model = new Model(id, UNIT, "Laptop X1", "Acme", 24, 72, Set.of(), null, null, com.zera.ms_inventory.Fixtures.category(CATEGORY_ID, UNIT));
        when(updateModelExpectedLifespanMonths.execute(UNIT, id, 72)).thenReturn(model);

        mockMvc.perform(patch("/api/v1/models/{id}/expected-lifespan-months", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateModelExpectedLifespanMonthsRequest(72)))
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expectedLifespanMonths").value(72));
    }

    @Test
    @DisplayName("PATCH /api/v1/models/{id}/materials - deve trocar os materiais do catalogo")
    void shouldUpdateMaterials() throws Exception {
        UUID id = UUID.randomUUID();
        Material battery = new Material(UUID.randomUUID(), MaterialCode.BATTERY, "Pilhas e baterias", true, true, "guia");
        Model model = new Model(id, UNIT, "Laptop X1", "Acme", 24, 60, Set.of(battery), null, null,
                com.zera.ms_inventory.Fixtures.category(CATEGORY_ID, UNIT));
        when(updateModelMaterials.execute(UNIT, id, Set.of(MaterialCode.BATTERY))).thenReturn(model);

        mockMvc.perform(patch("/api/v1/models/{id}/materials", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateModelMaterialsRequest(Set.of(MaterialCode.BATTERY))))
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materials[0].code").value("BATTERY"))
                .andExpect(jsonPath("$.hazardous").value(true));
    }

    @Test
    @DisplayName("PATCH /api/v1/models/{id}/materials - deve retornar 400 sem materiais")
    void shouldReturn400WhenMaterialsAreEmpty() throws Exception {
        mockMvc.perform(patch("/api/v1/models/{id}/materials", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"materials\":[]}")
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/models - deve retornar 400 sem materiais e com peso nao positivo")
    void shouldReturn400WithoutMaterialsOrWithNonPositiveWeight() throws Exception {
        mockMvc.perform(post("/api/v1/models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateModelRequest("Laptop X1", "Acme", null, null, Set.of(), null, null, CATEGORY_ID)))
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateModelRequest("Laptop X1", "Acme", null, null, Set.of(MaterialCode.METAL), 0.0, null, CATEGORY_ID)))
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/v1/models/{id} - deve remover o model e retornar 204")
    void shouldDeleteModel() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/models/{id}", id)
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isNoContent());

        verify(deleteModel).execute(UNIT, id);
    }

    @Test
    @DisplayName("GET /api/v1/models - deve repassar a pagina e o tamanho pedidos")
    void shouldForwardRequestedPage() throws Exception {
        when(listModels.execute(UNIT, null, new Pagination(2, 50))).thenReturn(new PageResult<>(List.of(), 2, 50, 101));

        mockMvc.perform(get("/api/v1/models")
                        .param("page", "2")
                        .param("size", "50")
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    @DisplayName("GET /api/v1/models - deve retornar 400 quando o tamanho passar do maximo")
    void shouldRejectPageSizeAboveTheMaximum() throws Exception {
        mockMvc.perform(get("/api/v1/models")
                        .param("size", "500")
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/models?approvalStatus=PENDING - deve filtrar os modelos pendentes")
    void shouldFilterModelsByApprovalStatus() throws Exception {
        when(listModels.execute(UNIT, ApprovalStatus.PENDING, new Pagination(0, 20)))
                .thenReturn(new PageResult<>(List.of(com.zera.ms_inventory.Fixtures.model(UNIT)), 0, 20, 1));

        mockMvc.perform(get("/api/v1/models")
                        .param("approvalStatus", "PENDING")
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/models/{id}/items - deve listar os itens do modelo")
    void shouldListTheItemsOfAModel() throws Exception {
        UUID id = UUID.randomUUID();
        Item item = com.zera.ms_inventory.Fixtures.item(UNIT);
        when(listModelItems.execute(UNIT, id, new Pagination(0, 20))).thenReturn(new PageResult<>(List.of(item), 0, 20, 1));

        mockMvc.perform(get("/api/v1/models/{id}/items", id)
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].barcode").value("7891234567890"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/models/{id}/items - deve retornar 404 para modelo de outra unidade")
    void shouldReturn404ForItemsOfAMissingModel() throws Exception {
        UUID id = UUID.randomUUID();
        when(listModelItems.execute(UNIT, id, new Pagination(0, 20))).thenThrow(new ModelNotFoundException(id));

        mockMvc.perform(get("/api/v1/models/{id}/items", id)
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/models/{id} - deve retornar 409 quando o modelo ainda tiver itens")
    void shouldReturn409WhenDeletingAModelWithItems() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ModelInUseException(id)).when(deleteModel).execute(UNIT, id);

        mockMvc.perform(delete("/api/v1/models/{id}", id)
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isConflict());
    }
}
