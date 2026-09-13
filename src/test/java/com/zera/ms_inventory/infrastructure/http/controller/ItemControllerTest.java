package com.zera.ms_inventory.infrastructure.http.controller;

import java.time.LocalDate;
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
import com.zera.ms_inventory.core.domain.valueobject.ItemFilter;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.domain.valueobject.UsageIntensity;
import com.zera.ms_inventory.core.usecase.item.AssignItemUnit;
import com.zera.ms_inventory.core.usecase.item.CreateItem;
import com.zera.ms_inventory.core.usecase.item.CreateItemCommand;
import com.zera.ms_inventory.core.usecase.item.CreateItemResult;
import com.zera.ms_inventory.core.usecase.item.DeleteItem;
import com.zera.ms_inventory.core.usecase.item.FindItemByBarcode;
import com.zera.ms_inventory.core.usecase.item.FindItemById;
import com.zera.ms_inventory.core.usecase.item.UpdateItem;
import com.zera.ms_inventory.core.usecase.item.UpdateItemCommand;
import com.zera.ms_inventory.core.usecase.item.ListItems;
import com.zera.ms_inventory.core.usecase.item.UpdateItemStatus;
import com.zera.ms_inventory.infrastructure.http.handler.GlobalExceptionHandler;
import com.zera.ms_inventory.infrastructure.http.request.AssignItemUnitRequest;
import com.zera.ms_inventory.infrastructure.http.request.CreateItemRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateItemStatusRequest;

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
    @MockitoBean private FindItemByBarcode findItemByBarcode;
    @MockitoBean private UpdateItem updateItem;
    @MockitoBean private UpdateItemStatus updateItemStatus;
    @MockitoBean private AssignItemUnit assignItemUnit;
    @MockitoBean private DeleteItem deleteItem;

    private Item sampleItem(UUID id) {
        return new Item(id, new Barcode("123456"), ItemStatus.OK, UNIT,
                com.zera.ms_inventory.Fixtures.model(MODEL_ID, UNIT), null,
                2024, UsageIntensity.MEDIUM, "SN-001", LocalDate.now());
    }

    @Test
    @DisplayName("POST /api/v1/items - deve criar item e retornar 201")
    void shouldCreateItem() throws Exception {
        UUID id = UUID.randomUUID();
        Item item = sampleItem(id);
        when(createItem.execute(any(CreateItemCommand.class))).thenReturn(new CreateItemResult(item, true));

        CreateItemRequest request = new CreateItemRequest(null, "123456", ItemStatus.OK, MODEL_ID, null,
                2024, UsageIntensity.MEDIUM, "SN-001", LocalDate.now(), "Placa de vídeo", ItemCondition.USED, false, Set.of(), null);

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

        CreateItemRequest request = new CreateItemRequest(null, "123456", ItemStatus.OK, MODEL_ID, null,
                2024, UsageIntensity.MEDIUM, "SN-001", LocalDate.now(), "Placa de vídeo", ItemCondition.USED, false, Set.of(), null);

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
        CreateItemRequest request = new CreateItemRequest(null, "", ItemStatus.OK, MODEL_ID, null,
                2024, UsageIntensity.MEDIUM, "SN-001", LocalDate.now(), "Placa de vídeo", ItemCondition.USED, false, Set.of(), null);

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
        when(listItems.execute(UNIT, ItemFilter.none(), new Pagination(0, 20))).thenReturn(new PageResult<>(List.of(item), 0, 20, 1));

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
        when(listItems.execute(UNIT, ItemFilter.none(), new Pagination(2, 50))).thenReturn(new PageResult<>(List.of(), 2, 50, 101));

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
                        && command.damages().equals(Set.of(DamageType.OXIDATION))))).thenReturn(new CreateItemResult(item, true));

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

    @Test
    @DisplayName("PATCH /api/v1/items/{id} - deve editar so os campos enviados")
    void shouldPartiallyUpdateTheItem() throws Exception {
        UUID id = UUID.randomUUID();
        Item item = sampleItem(id);
        item.describe("Placa de vídeo", ItemCondition.DAMAGED, true, Set.of(DamageType.DOES_NOT_POWER_ON), null);
        when(updateItem.execute(new UpdateItemCommand(UNIT, id, null, ItemCondition.DAMAGED, true,
                Set.of(DamageType.DOES_NOT_POWER_ON), null, null, null, null, null))).thenReturn(item);

        mockMvc.perform(patch("/api/v1/items/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"condition\":\"DAMAGED\",\"hasDamages\":true,\"damages\":[\"DOES_NOT_POWER_ON\"]}")
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.condition").value("DAMAGED"))
                .andExpect(jsonPath("$.damages[0]").value("DOES_NOT_POWER_ON"));
    }

    @Test
    @DisplayName("PATCH /api/v1/items/{id} - deve retornar 400 para nome vazio")
    void shouldReturn400ForAnEmptyName() throws Exception {
        mockMvc.perform(patch("/api/v1/items/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}")
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/items/by-barcode/{barcode} - deve retornar o item lido pelo scanner")
    void shouldFindItemByBarcode() throws Exception {
        Item item = sampleItem(UUID.randomUUID());
        item.assignDisplayCode("265964");
        when(findItemByBarcode.execute(UNIT, "123456")).thenReturn(item);

        mockMvc.perform(get("/api/v1/items/by-barcode/{barcode}", "123456")
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.barcode").value("123456"))
                .andExpect(jsonPath("$.displayCode").value("265964"));
    }

    @Test
    @DisplayName("GET /api/v1/items/by-barcode/{barcode} - deve retornar 404 para barcode desconhecido")
    void shouldReturn404ForUnknownBarcode() throws Exception {
        when(findItemByBarcode.execute(UNIT, "111111-J")).thenThrow(ItemNotFoundException.withBarcode("111111-J"));

        mockMvc.perform(get("/api/v1/items/by-barcode/{barcode}", "111111-J")
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/items - deve devolver 200 com o item ja cadastrado no reenvio do mesmo id")
    void shouldReturn200WhenTheSameIdIsResent() throws Exception {
        UUID id = UUID.randomUUID();
        when(createItem.execute(any(CreateItemCommand.class))).thenReturn(new CreateItemResult(sampleItem(id), false));

        mockMvc.perform(post("/api/v1/items")
                        .principal(new TestingAuthenticationToken(OPERATOR_ID.toString(), null, "ROLE_EMPLOYEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"%s\",\"barcode\":\"123456\",\"modelId\":\"%s\"}".formatted(id, MODEL_ID))
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @DisplayName("POST /api/v1/items - deve criar o modelo junto quando vier model no lugar de modelId")
    void shouldForwardTheNewModelToBeCreatedTogether() throws Exception {
        UUID categoryId = UUID.randomUUID();
        when(createItem.execute(org.mockito.ArgumentMatchers.argThat(command -> command.modelId() == null
                && command.newModel() != null
                && command.newModel().name().equals("Placa de vídeo")
                && command.newModel().categoryId().equals(categoryId)
                && command.newModel().actor().userId().equals(OPERATOR_ID))))
                .thenReturn(new CreateItemResult(sampleItem(UUID.randomUUID()), true));

        mockMvc.perform(post("/api/v1/items")
                        .principal(new TestingAuthenticationToken(OPERATOR_ID.toString(), null, "ROLE_EMPLOYEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"barcode":"123456","model":{"name":"Placa de vídeo","manufacturer":"Nvidia",
                                 "materials":["CIRCUIT_BOARD"],"categoryId":"%s"}}
                                """.formatted(categoryId))
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/v1/items - deve retornar 400 sem modelo, com os dois ou com modelo novo invalido")
    void shouldReturn400UnlessExactlyOneValidModelIsInformed() throws Exception {
        String[] bodies = {
                "{\"barcode\":\"123456\"}",
                "{\"barcode\":\"123456\",\"modelId\":\"%s\",\"model\":{\"name\":\"X\",\"manufacturer\":\"Y\",\"materials\":[\"METAL\"],\"categoryId\":\"%s\"}}"
                        .formatted(MODEL_ID, UUID.randomUUID()),
                "{\"barcode\":\"123456\",\"model\":{\"name\":\"X\",\"manufacturer\":\"Y\",\"materials\":[],\"categoryId\":\"%s\"}}"
                        .formatted(UUID.randomUUID())
        };
        for (String body : bodies) {
            mockMvc.perform(post("/api/v1/items")
                            .principal(new TestingAuthenticationToken(OPERATOR_ID.toString(), null, "ROLE_EMPLOYEE"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .header("X-Unit-Id", UNIT))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("POST /api/v1/items - deve retornar 409 quando o id do app pertence a outra unidade")
    void shouldReturn409ForAnIdFromAnotherUnit() throws Exception {
        UUID id = UUID.randomUUID();
        when(createItem.execute(any(CreateItemCommand.class)))
                .thenThrow(new com.zera.ms_inventory.core.domain.exception.ItemIdInUseException(id));

        mockMvc.perform(post("/api/v1/items")
                        .principal(new TestingAuthenticationToken(OPERATOR_ID.toString(), null, "ROLE_EMPLOYEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"%s\",\"barcode\":\"123456\",\"modelId\":\"%s\"}".formatted(id, MODEL_ID))
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /api/v1/items - deve repassar filtros e busca da tela Itens")
    void shouldForwardFiltersAndSearch() throws Exception {
        UUID categoryId = UUID.randomUUID();
        ItemFilter filter = new ItemFilter(ItemStatus.OK, categoryId, MODEL_ID, "265964");
        when(listItems.execute(UNIT, filter, new Pagination(0, 20)))
                .thenReturn(new PageResult<>(List.of(sampleItem(UUID.randomUUID())), 0, 20, 1));

        mockMvc.perform(get("/api/v1/items")
                        .param("status", "OK")
                        .param("categoryId", categoryId.toString())
                        .param("modelId", MODEL_ID.toString())
                        .param("q", " 265964 ")
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
