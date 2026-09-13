package com.zera.ms_inventory.infrastructure.http.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.core.domain.valueobject.DamageType;
import com.zera.ms_inventory.core.domain.valueobject.ItemCondition;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.usecase.item.AssignItemUnit;
import com.zera.ms_inventory.core.usecase.item.CreateItem;
import com.zera.ms_inventory.core.usecase.item.CreateItemCommand;
import com.zera.ms_inventory.core.usecase.item.DeleteItem;
import com.zera.ms_inventory.core.usecase.item.FindItemById;
import com.zera.ms_inventory.core.usecase.item.ListItems;
import com.zera.ms_inventory.core.usecase.item.UpdateItemAcquiredAt;
import com.zera.ms_inventory.core.usecase.item.UpdateItemManufacturingDate;
import com.zera.ms_inventory.core.usecase.item.UpdateItemNextPredictionDate;
import com.zera.ms_inventory.core.usecase.item.UpdateItemSerialNumber;
import com.zera.ms_inventory.core.usecase.item.UpdateItemStatus;
import com.zera.ms_inventory.core.usecase.item.UpdateItemUsageIntensity;
import com.zera.ms_inventory.infrastructure.http.handler.GlobalExceptionHandler;
import com.zera.ms_inventory.infrastructure.http.request.AssignItemUnitRequest;
import com.zera.ms_inventory.infrastructure.http.request.CreateItemRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateItemAcquiredAtRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateItemManufacturingDateRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateItemNextPredictionDateRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateItemSerialNumberRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateItemStatusRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateItemUsageIntensityRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
@org.springframework.context.annotation.Import(GlobalExceptionHandler.class)
class ItemControllerTest {


    private static final UUID OPERATOR_ID = UUID.fromString("00000000-0000-0000-0000-0000000000e1");
    private static final UUID UNIT = com.zera.ms_inventory.Fixtures.UNIT;
    private static final UUID MODEL_ID = UUID.fromString("00000000-0000-0000-0000-0000000000d4");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private CreateItem createItem;
    @MockitoBean private ListItems listItems;
    @MockitoBean private FindItemById findItemById;
    @MockitoBean private UpdateItemStatus updateItemStatus;
    @MockitoBean private AssignItemUnit assignItemUnit;
    @MockitoBean private UpdateItemSerialNumber updateItemSerialNumber;
    @MockitoBean private UpdateItemAcquiredAt updateItemAcquiredAt;
    @MockitoBean private UpdateItemNextPredictionDate updateItemNextPredictionDate;
    @MockitoBean private UpdateItemManufacturingDate updateItemManufacturingDate;
    @MockitoBean private UpdateItemUsageIntensity updateItemUsageIntensity;
    @MockitoBean private DeleteItem deleteItem;

    private Item sampleItem(UUID id) {
        return new Item(id, new Barcode("123456"), ItemStatus.OK, UNIT,
                com.zera.ms_inventory.Fixtures.model(MODEL_ID, UNIT), LocalDateTime.now(),
                12, 5, "SN-001", LocalDate.now());
    }

    @Test
    @DisplayName("POST /api/v1/items - deve criar item e retornar 201")
    void shouldCreateItem() throws Exception {
        UUID id = UUID.randomUUID();
        Item item = sampleItem(id);
        when(createItem.execute(any(CreateItemCommand.class))).thenReturn(item);

        CreateItemRequest request = new CreateItemRequest("123456", ItemStatus.OK, MODEL_ID,
                LocalDateTime.now(), 12, 5, "SN-001", LocalDate.now(), "Placa de vídeo", ItemCondition.USED, false, Set.of(), null);

        mockMvc.perform(post("/api/v1/items")
                        .principal(new TestingAuthenticationToken(OPERATOR_ID.toString(), null, "ROLE_EMPLOYEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.barcode").value("123456"));
    }

    @Test
    @DisplayName("POST /api/v1/items - deve retornar 409 quando o barcode ja existir na unidade")
    void shouldReturn409WhenBarcodeAlreadyExistsInTheUnit() throws Exception {
        when(createItem.execute(any(CreateItemCommand.class)))
                .thenThrow(new DataIntegrityViolationException("Node already exists with label `Item`"));

        CreateItemRequest request = new CreateItemRequest("123456", ItemStatus.OK, MODEL_ID,
                LocalDateTime.now(), 12, 5, "SN-001", LocalDate.now(), "Placa de vídeo", ItemCondition.USED, false, Set.of(), null);

        mockMvc.perform(post("/api/v1/items")
                        .principal(new TestingAuthenticationToken(OPERATOR_ID.toString(), null, "ROLE_EMPLOYEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("A record with the same unique value already exists"));
    }

    @Test
    @DisplayName("POST /api/v1/items - deve retornar 400 quando o barcode estiver em branco")
    void shouldReturn400WhenBarcodeIsBlank() throws Exception {
        CreateItemRequest request = new CreateItemRequest("", ItemStatus.OK, MODEL_ID,
                LocalDateTime.now(), 12, 5, "SN-001", LocalDate.now(), "Placa de vídeo", ItemCondition.USED, false, Set.of(), null);

        mockMvc.perform(post("/api/v1/items")
                        .principal(new TestingAuthenticationToken(OPERATOR_ID.toString(), null, "ROLE_EMPLOYEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/items - deve listar todos os itens")
    void shouldFindAllItems() throws Exception {
        Item item = sampleItem(UUID.randomUUID());
        when(listItems.execute(UNIT, new Pagination(0, 20))).thenReturn(new PageResult<>(List.of(item), 0, 20, 1));

        mockMvc.perform(get("/api/v1/items")
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].barcode").value("123456"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/items/{id} - deve retornar o item")
    void shouldFindItemById() throws Exception {
        UUID id = UUID.randomUUID();
        Item item = sampleItem(id);
        when(findItemById.execute(UNIT, id)).thenReturn(item);

        mockMvc.perform(get("/api/v1/items/{id}", id)
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/items/{id} - deve retornar 404 quando o item não existir")
    void shouldReturn404WhenItemDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        when(findItemById.execute(UNIT, id)).thenThrow(new ItemNotFoundException(id));

        mockMvc.perform(get("/api/v1/items/{id}", id)
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/v1/items/{id}/status - deve atualizar o status")
    void shouldUpdateStatus() throws Exception {
        UUID id = UUID.randomUUID();
        Item item = sampleItem(id);
        when(updateItemStatus.execute(UNIT, id, ItemStatus.DAMAGED)).thenReturn(item);

        mockMvc.perform(patch("/api/v1/items/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateItemStatusRequest(ItemStatus.DAMAGED)))
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/v1/items/{id}/unit - deve reatribuir a unidade")
    void shouldAssignUnit() throws Exception {
        UUID id = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        Item item = sampleItem(id);
        when(assignItemUnit.execute(UNIT, id, unitId)).thenReturn(item);

        mockMvc.perform(patch("/api/v1/items/{id}/unit", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AssignItemUnitRequest(unitId)))
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/v1/items/{id}/serial-number - deve atualizar o número de série")
    void shouldUpdateSerialNumber() throws Exception {
        UUID id = UUID.randomUUID();
        Item item = sampleItem(id);
        when(updateItemSerialNumber.execute(UNIT, id, "SN-002")).thenReturn(item);

        mockMvc.perform(patch("/api/v1/items/{id}/serial-number", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateItemSerialNumberRequest("SN-002")))
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/v1/items/{id}/acquired-at - deve atualizar a data de aquisição")
    void shouldUpdateAcquiredAt() throws Exception {
        UUID id = UUID.randomUUID();
        Item item = sampleItem(id);
        LocalDate newDate = LocalDate.now();
        when(updateItemAcquiredAt.execute(UNIT, id, newDate)).thenReturn(item);

        mockMvc.perform(patch("/api/v1/items/{id}/acquired-at", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateItemAcquiredAtRequest(newDate)))
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/v1/items/{id}/next-prediction-date - deve atualizar a data de previsão")
    void shouldUpdateNextPredictionDate() throws Exception {
        UUID id = UUID.randomUUID();
        Item item = sampleItem(id);
        LocalDateTime newDate = LocalDateTime.now();
        when(updateItemNextPredictionDate.execute(UNIT, id, newDate)).thenReturn(item);

        mockMvc.perform(patch("/api/v1/items/{id}/next-prediction-date", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateItemNextPredictionDateRequest(newDate)))
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/v1/items/{id}/manufacturing-date - deve atualizar o ano de fabricação")
    void shouldUpdateManufacturingDate() throws Exception {
        UUID id = UUID.randomUUID();
        Item item = sampleItem(id);
        when(updateItemManufacturingDate.execute(UNIT, id, 2024)).thenReturn(item);

        mockMvc.perform(patch("/api/v1/items/{id}/manufacturing-date", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateItemManufacturingDateRequest(2024)))
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/v1/items/{id}/usage-intensity - deve atualizar a intensidade de uso")
    void shouldUpdateUsageIntensity() throws Exception {
        UUID id = UUID.randomUUID();
        Item item = sampleItem(id);
        when(updateItemUsageIntensity.execute(UNIT, id, 8)).thenReturn(item);

        mockMvc.perform(patch("/api/v1/items/{id}/usage-intensity", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateItemUsageIntensityRequest(8)))
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/items/{id} - deve remover o item e retornar 204")
    void shouldDeleteItem() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/items/{id}", id)
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isNoContent());

        verify(deleteItem).execute(UNIT, id);
    }

    @Test
    @DisplayName("GET /api/v1/items - deve repassar a pagina e o tamanho pedidos")
    void shouldForwardRequestedPage() throws Exception {
        when(listItems.execute(UNIT, new Pagination(2, 50))).thenReturn(new PageResult<>(List.of(), 2, 50, 101));

        mockMvc.perform(get("/api/v1/items")
                        .param("page", "2")
                        .param("size", "50")
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    @DisplayName("GET /api/v1/items - deve retornar 400 quando o tamanho passar do maximo")
    void shouldRejectPageSizeAboveTheMaximum() throws Exception {
        mockMvc.perform(get("/api/v1/items")
                        .param("size", "500")
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/items - deve repassar os dados do cadastro e o autor do token")
    void shouldForwardRegistrationDataAndAuthor() throws Exception {
        Item item = sampleItem(UUID.randomUUID());
        item.describe("Placa de vídeo", ItemCondition.SEMI_DAMAGED, true, Set.of(DamageType.OXIDATION), "Pino torto");
        item.registerBy(new com.zera.ms_inventory.core.domain.valueobject.Actor(OPERATOR_ID,
                com.zera.ms_inventory.core.domain.valueobject.ActorRole.EMPLOYEE, "Gustavo Macal"));
        when(createItem.execute(org.mockito.ArgumentMatchers.argThat(command ->
                command.actor().userId().equals(OPERATOR_ID)
                        && command.condition() == ItemCondition.SEMI_DAMAGED
                        && command.damages().equals(Set.of(DamageType.OXIDATION))))).thenReturn(item);

        mockMvc.perform(post("/api/v1/items")
                        .principal(new TestingAuthenticationToken(OPERATOR_ID.toString(), null, "ROLE_EMPLOYEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"barcode":"123456","status":"OK","modelId":"%s","name":"Placa de vídeo",
                                 "condition":"SEMI_DAMAGED","hasDamages":true,"damages":["OXIDATION"],"notes":"Pino torto"}
                                """.formatted(MODEL_ID))
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Placa de vídeo"))
                .andExpect(jsonPath("$.condition").value("SEMI_DAMAGED"))
                .andExpect(jsonPath("$.damages[0]").value("OXIDATION"))
                .andExpect(jsonPath("$.createdByName").value("Gustavo Macal"));
    }
}
