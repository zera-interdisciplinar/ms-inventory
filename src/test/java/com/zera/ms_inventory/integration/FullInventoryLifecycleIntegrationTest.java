package com.zera.ms_inventory.integration;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.neo4j.Neo4jContainer;

import com.jayway.jsonpath.JsonPath;
import com.zera.ms_inventory.core.repository.PhotoStorage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Percorre o fluxo principal do inventario — cadastro, submissao, aprovacao, manutencao, descarte
 * e indicadores (ZERA-260) — contra um Neo4j real via Testcontainers, subindo pela API HTTP com
 * MockMvc (o mesmo mecanismo de autenticacao dos testes de RBAC), nao chamando casos de uso
 * diretamente. E o unico teste do projeto que roda as migrations de verdade: sem elas, o catalogo
 * de materiais da V002 nao existiria e a criacao do modelo falharia, exatamente o tipo de
 * regressao de schema que um teste com mock nunca pegaria.
 *
 * <p>{@link TestMethodOrder} porque cada metodo continua o estado do anterior (o mesmo item avanca
 * pela maquina de estados) — nao e um teste parametrizado de casos independentes, e uma novela.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@TestPropertySource(properties = "zera.neo4j.migrations.enabled=true")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FullInventoryLifecycleIntegrationTest {

    @Container
    @ServiceConnection
    static final Neo4jContainer NEO4J = new Neo4jContainer("neo4j:5-community")
            .withoutAuthentication()
            // a imagem some cold + subida do Neo4j passa dos 60s padrao em CI/ambientes mais lentos
            .withStartupTimeout(java.time.Duration.ofMinutes(5));

    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {
        // @ServiceConnection ja configura uri/driver; so o nome do banco falta, e o driver do
        // Neo4j exige autenticacao mesmo com o servidor "sem senha" (usuario/senha default)
        registry.add("spring.data.neo4j.database", () -> "neo4j");
    }

    @Autowired
    private MockMvc mockMvc;

    /**
     * O storage real e GCS; sem bucket configurado (nenhum ambiente de teste tem um), o upload
     * responde 503 de proposito. A foto nao e mais obrigatoria para submeter, mas o passo continua
     * no roteiro porque anexar foto e parte do fluxo real — entao o armazenamento e substituido
     * aqui por um fake que so devolve uma URL.
     */
    @MockitoBean
    private PhotoStorage photoStorage;

    private static final UUID UNIT = UUID.randomUUID();

    private static UUID categoryId;
    private static UUID modelId;
    private static UUID itemId;
    private static UUID disposalId;

    private static <B extends org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder<B>>
            B asRole(B request, String role, UUID subject) {
        return request.with(jwt()
                .jwt(b -> b.subject(subject.toString()).claim("name", role + " de teste"))
                .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role)));
    }

    private static final UUID MANAGER_ID = UUID.randomUUID();
    private static final UUID EMPLOYEE_ID = UUID.randomUUID();

    @BeforeEach
    void stubPhotoStorage() throws Exception {
        when(photoStorage.store(anyString(), any(), anyString())).thenReturn("items/fake.jpg");
        when(photoStorage.signedUrl(anyString())).thenReturn(java.util.Optional.of(
                java.net.URI.create("https://fake.local/items/fake.jpg").toURL()));
    }

    @Test
    @Order(1)
    void managerCadastraACategoria() throws Exception {
        String body = mockMvc.perform(asRole(post("/api/v1/categories"), "MANAGER", MANAGER_ID)
                        .header("X-Unit-Id", UNIT)
                        .contentType("application/json")
                        .content("""
                                {"name": "Notebooks", "description": "Notebooks e laptops"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        categoryId = UUID.fromString(JsonPath.read(body, "$.id"));
    }

    /** Materiais vem da V002; sem migrations rodando de verdade, este passo falharia sozinho. */
    @Test
    @Order(2)
    void operarioCadastraOModelo() throws Exception {
        String body = mockMvc.perform(asRole(post("/api/v1/models"), "EMPLOYEE", EMPLOYEE_ID)
                        .header("X-Unit-Id", UNIT)
                        .contentType("application/json")
                        .content("""
                                {"name": "Latitude 5420", "manufacturer": "Dell", "warrantyMonths": 12,
                                 "expectedLifespanMonths": 48, "materials": ["METAL", "PLASTIC"],
                                 "estimatedWeightKg": 1.8, "categoryId": "%s"}
                                """.formatted(categoryId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        modelId = UUID.fromString(JsonPath.read(body, "$.id"));
    }

    @Test
    @Order(3)
    void operarioCadastraOItemComoRascunho() throws Exception {
        String body = mockMvc.perform(asRole(post("/api/v1/items"), "EMPLOYEE", EMPLOYEE_ID)
                        .header("X-Unit-Id", UNIT)
                        .contentType("application/json")
                        .content("""
                                {"barcode": "7891234500000", "modelId": "%s", "name": "Notebook do descarte",
                                 "manufacturingYear": 2022, "usageIntensity": 4, "condition": "NEW",
                                 "hasDamages": false, "acquiredAt": "2022-06-01"}
                                """.formatted(modelId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn().getResponse().getContentAsString();
        itemId = UUID.fromString(JsonPath.read(body, "$.id"));
    }

    /** Foto e opcional para submeter; anexar segue funcionando e devolve a URL assinada. */
    @Test
    @Order(4)
    void operarioAnexaAFoto() throws Exception {
        var photo = new org.springframework.mock.web.MockMultipartFile(
                "photo", "notebook.jpg", "image/jpeg", new byte[] {1, 2, 3, 4});
        mockMvc.perform(asRole(multipart("/api/v1/items/{id}/photo", itemId), "EMPLOYEE", EMPLOYEE_ID)
                        .file(photo)
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.photoUrl").isNotEmpty());
    }

    /** Enviado por operario: vai para a fila do gestor, nao direto pro estoque. */
    @Test
    @Order(5)
    void operarioSubmeteEVaiParaAprovacao() throws Exception {
        mockMvc.perform(asRole(post("/api/v1/items/{id}/submit", itemId), "EMPLOYEE", EMPLOYEE_ID)
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"));
    }

    @Test
    @Order(6)
    void gestorAprovaEOItemEntraNoEstoque() throws Exception {
        mockMvc.perform(asRole(post("/api/v1/items/{id}/approve", itemId), "MANAGER", MANAGER_ID)
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_STOCK"));
    }

    @Test
    @Order(7)
    void operarioEnviaOItemParaManutencao() throws Exception {
        mockMvc.perform(asRole(post("/api/v1/items/{id}/maintenance/start", itemId), "EMPLOYEE", EMPLOYEE_ID)
                        .header("X-Unit-Id", UNIT)
                        .contentType("application/json")
                        .content("""
                                {"reason": "Bateria nao carrega"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_MAINTENANCE"));
    }

    @Test
    @Order(8)
    void operarioFinalizaAManutencao() throws Exception {
        mockMvc.perform(asRole(post("/api/v1/items/{id}/maintenance/finish", itemId), "EMPLOYEE", EMPLOYEE_ID)
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AWAITING_EVALUATION"));
    }

    /** Avaliado como ainda usavel: volta ao estoque em vez de ir direto para descarte. */
    @Test
    @Order(9)
    void operarioAvaliaEOItemVoltaAoEstoque() throws Exception {
        mockMvc.perform(asRole(post("/api/v1/items/{id}/evaluate", itemId), "EMPLOYEE", EMPLOYEE_ID)
                        .header("X-Unit-Id", UNIT)
                        .contentType("application/json")
                        .content("""
                                {"condition": "USED", "hasDamages": false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_STOCK"));
    }

    @Test
    @Order(10)
    void operarioRegistraODescarteParaReciclagem() throws Exception {
        String body = mockMvc.perform(asRole(post("/api/v1/disposals"), "EMPLOYEE", EMPLOYEE_ID)
                        .header("X-Unit-Id", UNIT)
                        .contentType("application/json")
                        .content("""
                                {"destination": "RECYCLING", "placeName": "Recicladora Central",
                                 "disposedAt": "%s", "itemIds": ["%s"]}
                                """.formatted(java.time.LocalDate.now(), itemId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        disposalId = UUID.fromString(JsonPath.read(body, "$.id"));

        mockMvc.perform(asRole(get("/api/v1/items/{id}", itemId), "EMPLOYEE", EMPLOYEE_ID)
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISPOSED"));
    }

    @Test
    @Order(11)
    void oDescarteApareceNosIndicadoresDaUnidade() throws Exception {
        String today = java.time.LocalDate.now().toString();
        mockMvc.perform(asRole(get("/api/v1/dashboard/indicators"), "MANAGER", MANAGER_ID)
                        .header("X-Unit-Id", UNIT)
                        .param("from", today)
                        .param("to", today))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalWeightKg").value(1.8))
                .andExpect(jsonPath("$.recyclingRatePercent").value(100.0));
    }

    /**
     * O painel inicial soma o que acabou de acontecer: uma unidade com um unico item, ja
     * descartado, nao tem mais nada ativo, e o descarte recente aparece na janela.
     */
    @Test
    @Order(12)
    void oPainelInicialReflete_o_estadoFinalDaUnidade() throws Exception {
        mockMvc.perform(asRole(get("/api/v1/dashboard/home"), "MANAGER", MANAGER_ID)
                        .header("X-Unit-Id", UNIT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeItems").value(0))
                .andExpect(jsonPath("$.pendingApproval").value(0))
                .andExpect(jsonPath("$.disposalsInWindow").value(1));

        assertThat(disposalId).isNotNull();
    }
}
