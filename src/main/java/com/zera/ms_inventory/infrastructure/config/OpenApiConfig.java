package com.zera.ms_inventory.infrastructure.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("ms-inventory")
                .description("API de gestão de inventário: categorias, modelos, materiais, itens "
                        + "(com ciclo de vida e manutenção), descartes, regras e dashboard.")
                .version("v1"))
            // fixo e relativo: sem isso o springdoc preenche com o host da propria requisicao, e o
            // contrato versionado (docs/openapi.json) mudaria a cada ambiente que o gerasse
            .servers(List.of(new Server().url("/")));
    }
}
