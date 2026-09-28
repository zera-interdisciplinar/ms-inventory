package com.zera.ms_inventory.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiConfigTest {

    private OpenApiConfig config;

    @BeforeEach
    void setUp() {
        config = new OpenApiConfig();
    }

    @Test
    @DisplayName("Should create OpenAPI bean")
    void shouldCreateOpenApiBean() {
        assertNotNull(config.openAPI());
    }

    @Test
    @DisplayName("Should set correct title")
    void shouldSetCorrectTitle() {
        OpenAPI openAPI = config.openAPI();
        assertEquals("ms-inventory", openAPI.getInfo().getTitle());
    }

    @Test
    @DisplayName("Should set correct version")
    void shouldSetCorrectVersion() {
        OpenAPI openAPI = config.openAPI();
        assertEquals("v1", openAPI.getInfo().getVersion());
    }

    @Test
    @DisplayName("Should set description")
    void shouldSetDescription() {
        OpenAPI openAPI = config.openAPI();
        assertNotNull(openAPI.getInfo().getDescription());
    }

    /** Fixo e relativo, para o contrato versionado nao depender do host que o gerou. */
    @Test
    @DisplayName("Should set a fixed relative server url")
    void shouldSetAFixedRelativeServerUrl() {
        OpenAPI openAPI = config.openAPI();
        assertEquals(1, openAPI.getServers().size());
        assertEquals("/", openAPI.getServers().get(0).getUrl());
    }
}
