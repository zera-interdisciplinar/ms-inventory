package com.zera.ms_inventory.infrastructure.http.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import com.zera.ms_inventory.core.domain.entity.Category;
import com.zera.ms_inventory.core.domain.exception.CategoryNotFoundException;
import com.zera.ms_inventory.core.usecase.category.CreateCategory;
import com.zera.ms_inventory.core.usecase.category.DeleteCategory;
import com.zera.ms_inventory.core.usecase.category.FindAllCategories;
import com.zera.ms_inventory.core.usecase.category.FindCategoryById;
import com.zera.ms_inventory.core.usecase.category.UpdateCategoryDescription;
import com.zera.ms_inventory.core.usecase.category.UpdateCategoryName;
import com.zera.ms_inventory.infrastructure.http.handler.GlobalExceptionHandler;
import com.zera.ms_inventory.infrastructure.http.request.CreateCategoryRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateCategoryDescriptionRequest;
import com.zera.ms_inventory.infrastructure.http.request.UpdateCategoryNameRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CategoryController.class)
@org.springframework.context.annotation.Import(GlobalExceptionHandler.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private CreateCategory createCategory;
    @MockitoBean private FindAllCategories findAllCategories;
    @MockitoBean private FindCategoryById findCategoryById;
    @MockitoBean private UpdateCategoryName updateCategoryName;
    @MockitoBean private UpdateCategoryDescription updateCategoryDescription;
    @MockitoBean private DeleteCategory deleteCategory;

    @Test
    @DisplayName("POST /api/v1/categories - deve criar categoria e retornar 201")
    void shouldCreateCategory() throws Exception {
        UUID id = UUID.randomUUID();
        Category category = new Category(id, "Electronics", "Devices");
        when(createCategory.execute(eq("Electronics"), eq("Devices"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(category);

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateCategoryRequest("Electronics", "Devices"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    @DisplayName("POST /api/v1/categories - deve retornar 400 quando o nome estiver em branco")
    void shouldReturn400WhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateCategoryRequest("", "Devices"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/categories - deve listar todas as categorias")
    void shouldFindAllCategories() throws Exception {
        Category category = new Category(UUID.randomUUID(), "Electronics", "Devices");
        when(findAllCategories.execute()).thenReturn(List.of(category));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Electronics"));
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} - deve retornar a categoria")
    void shouldFindCategoryById() throws Exception {
        UUID id = UUID.randomUUID();
        Category category = new Category(id, "Electronics", "Devices");
        when(findCategoryById.execute(id)).thenReturn(category);

        mockMvc.perform(get("/api/v1/categories/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} - deve retornar 404 quando a categoria não existir")
    void shouldReturn404WhenCategoryDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        when(findCategoryById.execute(id)).thenThrow(new CategoryNotFoundException(id));

        mockMvc.perform(get("/api/v1/categories/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/v1/categories/{id}/name - deve renomear a categoria")
    void shouldRenameCategory() throws Exception {
        UUID id = UUID.randomUUID();
        Category category = new Category(id, "Hardware", "Devices");
        when(updateCategoryName.execute(id, "Hardware")).thenReturn(category);

        mockMvc.perform(patch("/api/v1/categories/{id}/name", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateCategoryNameRequest("Hardware"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Hardware"));
    }

    @Test
    @DisplayName("PATCH /api/v1/categories/{id}/description - deve atualizar a descrição")
    void shouldUpdateCategoryDescription() throws Exception {
        UUID id = UUID.randomUUID();
        Category category = new Category(id, "Electronics", "Computer parts");
        when(updateCategoryDescription.execute(id, "Computer parts")).thenReturn(category);

        mockMvc.perform(patch("/api/v1/categories/{id}/description", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateCategoryDescriptionRequest("Computer parts"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Computer parts"));
    }

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} - deve remover a categoria e retornar 204")
    void shouldDeleteCategory() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/categories/{id}", id))
                .andExpect(status().isNoContent());

        verify(deleteCategory).execute(id);
    }
}
