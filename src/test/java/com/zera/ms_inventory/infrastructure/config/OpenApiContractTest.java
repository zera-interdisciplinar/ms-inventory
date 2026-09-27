package com.zera.ms_inventory.infrastructure.config;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * O contrato publicado em {@code docs/openapi.json} (ZERA-261) e o time do app le esse arquivo
 * versionado, nao o {@code /api-docs} ao vivo. Sem este teste, um endpoint novo ou um campo
 * renomeado ficaria fora do arquivo sem ninguem perceber ate o app quebrar em producao.
 *
 * <p>Compara o JSON estruturalmente (arvore, nao texto), entao a ordem de serializacao do
 * springdoc entre versoes nao derruba o teste por engano — so uma mudanca real de contrato
 * derruba. Quando o teste falhar de proposito (endpoint novo, campo alterado), regenere com:</p>
 *
 * <pre>
 * curl -s http://localhost:8089/api-docs | python3 -c "import json,sys; \
 *   json.dump(json.load(sys.stdin), open('docs/openapi.json','w'), indent=2, sort_keys=True, ensure_ascii=False)"
 * </pre>
 *
 * <p>Usa {@link HttpClient} puro em vez de {@code TestRestTemplate}: o modulo que o fornece nao
 * esta no classpath deste projeto, e trazer mais um so para uma chamada GET nao vale a pena.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class OpenApiContractTest {

    private static final Path COMMITTED_CONTRACT = Path.of("docs/openapi.json");

    @LocalServerPort
    private int port;

    @Test
    void theCommittedContractMustMatchWhatTheApplicationServes() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api-docs")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        JsonNode liveContract = mapper.readTree(response.body());

        assertThat(Files.exists(COMMITTED_CONTRACT))
                .as("docs/openapi.json deveria existir; veja o Javadoc desta classe para gerar")
                .isTrue();
        JsonNode committedContract = mapper.readTree(Files.readString(COMMITTED_CONTRACT));

        assertThat(committedContract)
                .as("docs/openapi.json ficou desatualizado; regenere-o (veja o Javadoc desta classe)")
                .isEqualTo(liveContract);
    }
}
