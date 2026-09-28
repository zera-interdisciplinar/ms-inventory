package com.zera.ms_inventory.infrastructure.admincore;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.RuleAlert;
import com.zera.ms_inventory.core.domain.valueobject.AlertSeverity;
import com.zera.ms_inventory.core.domain.valueobject.AlertSubject;
import com.zera.ms_inventory.core.domain.valueobject.RuleKind;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

/** Conversa com o admin-core contra um servidor simulado, sem subir o contexto. */
class AdminCoreIntegrationTest {

    private static final String TOKEN_BODY = """
            {"accessToken":"token-de-servico","tokenType":"Bearer","expiresIn":900}
            """;

    private AdminCoreProperties properties;
    private RestClient.Builder builder;
    private MockRestServiceServer server;
    private ServiceTokenProvider tokens;
    private AdminCoreClient client;

    @BeforeEach
    void setUp() {
        properties = new AdminCoreProperties(true, "http://admin-core", "ms-inventory", "segredo",
                Duration.ofSeconds(1), 2, Duration.ofMillis(1), Duration.ofMinutes(10));
        builder = RestClient.builder().baseUrl("http://admin-core");
        server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        tokens = new ServiceTokenProvider(restClient, properties);
        client = new AdminCoreClient(restClient, tokens, properties);
    }

    private void expectToken() {
        server.expect(requestTo("http://admin-core/api/v1/auth/service-token"))
                .andExpect(method(POST))
                .andExpect(jsonPath("$.clientId").value("ms-inventory"))
                .andExpect(jsonPath("$.clientSecret").value("segredo"))
                .andRespond(withSuccess(TOKEN_BODY, MediaType.APPLICATION_JSON));
    }

    // ---- token ----

    @Test
    void shouldFetchTheTokenOnceAndReuseIt() {
        expectToken();

        assertThat(tokens.token()).isEqualTo("token-de-servico");
        assertThat(tokens.token()).isEqualTo("token-de-servico");

        // uma unica ida ao admin-core; o servidor simulado reclamaria de uma segunda
        server.verify();
    }

    @Test
    void shouldFetchAgainAfterInvalidation() {
        expectToken();
        expectToken();

        tokens.token();
        tokens.invalidate();
        tokens.token();

        server.verify();
    }

    @Test
    void shouldFailWhenTheTokenDoesNotComeBack() {
        server.expect(requestTo("http://admin-core/api/v1/auth/service-token"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> tokens.token())
                .isInstanceOf(IllegalStateException.class);
    }

    // ---- destinatario ----

    @Test
    void shouldResolveTheUnitManagerAndCacheIt() {
        UUID managerId = UUID.randomUUID();
        expectToken();
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/api/v1/users")))
                .andExpect(method(GET))
                .andExpect(header("Authorization", "Bearer token-de-servico"))
                .andRespond(withSuccess("[{\"userId\":\"" + managerId + "\",\"name\":\"Kevin\"}]",
                        MediaType.APPLICATION_JSON));

        assertThat(client.managerOf(Fixtures.UNIT)).contains(managerId);
        // segunda chamada sai do cache; sem ele o servidor simulado reclamaria
        assertThat(client.managerOf(Fixtures.UNIT)).contains(managerId);
        server.verify();
    }

    @Test
    void shouldReturnEmptyWhenTheUnitHasNoManager() {
        expectToken();
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/api/v1/users")))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        assertThat(client.managerOf(Fixtures.UNIT)).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenTheLookupFails() {
        expectToken();
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/api/v1/users")))
                .andRespond(withServerError());

        assertThat(client.managerOf(Fixtures.UNIT)).isEmpty();
    }

    // ---- envio do alerta ----

    private RuleAlert alerta() {
        return new RuleAlert(Fixtures.UNIT, UUID.randomUUID(), RuleKind.WARRANTY_EXPIRATION,
                new AlertSubject(UUID.randomUUID(), "100001", "Notebook"), AlertSeverity.MEDIUM,
                "garantia vencendo", LocalDateTime.of(2026, 9, 26, 3, 0));
    }

    @Test
    void shouldPostTheAlertWithTheServiceToken() {
        UUID managerId = UUID.randomUUID();
        RuleAlert alerta = alerta();
        expectToken();
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/api/v1/users")))
                .andRespond(withSuccess("[{\"userId\":\"" + managerId + "\"}]", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://admin-core/api/v1/notifications/alerts"))
                .andExpect(method(POST))
                .andExpect(header("Authorization", "Bearer token-de-servico"))
                .andExpect(jsonPath("$.kind").value("WARRANTY_EXPIRATION"))
                .andExpect(jsonPath("$.severity").value("MEDIUM"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.userId").value(managerId.toString()))
                .andExpect(jsonPath("$.ruleId").value(alerta.ruleId().toString()))
                .andExpect(jsonPath("$.eventId").value(alerta.dedupSubject().toString()))
                .andRespond(withStatus(org.springframework.http.HttpStatus.ACCEPTED));

        assertThat(new AdminCoreAlertGateway(client, properties).send(alerta)).isTrue();
        server.verify();
    }

    /** Falha temporaria e tentada de novo; a segunda tentativa entrega. */
    @Test
    void shouldRetryAFailedDelivery() {
        UUID managerId = UUID.randomUUID();
        expectToken();
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/api/v1/users")))
                .andRespond(withSuccess("[{\"userId\":\"" + managerId + "\"}]", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://admin-core/api/v1/notifications/alerts"))
                .andRespond(withServerError());
        server.expect(requestTo("http://admin-core/api/v1/notifications/alerts"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.ACCEPTED));

        assertThat(new AdminCoreAlertGateway(client, properties).send(alerta())).isTrue();
        server.verify();
    }

    /** Esgotadas as tentativas, o alerta nao sai e a falha nao propaga. */
    @Test
    void shouldGiveUpAfterTheConfiguredAttempts() {
        UUID managerId = UUID.randomUUID();
        expectToken();
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/api/v1/users")))
                .andRespond(withSuccess("[{\"userId\":\"" + managerId + "\"}]", MediaType.APPLICATION_JSON));
        server.expect(org.springframework.test.web.client.ExpectedCount.times(2),
                        requestTo("http://admin-core/api/v1/notifications/alerts"))
                .andRespond(withServerError());

        assertThat(new AdminCoreAlertGateway(client, properties).send(alerta())).isFalse();
        server.verify();
    }

    @Test
    void shouldNotSendWithoutARecipient() {
        expectToken();
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/api/v1/users")))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        assertThat(new AdminCoreAlertGateway(client, properties).send(alerta())).isFalse();
    }

    @Test
    void shouldNotSendWhenTheIntegrationIsNotConfigured() {
        AdminCoreProperties semSegredo = new AdminCoreProperties(true, "http://admin-core", "ms-inventory",
                "", Duration.ofSeconds(1), 2, Duration.ofMillis(1), Duration.ofMinutes(10));

        assertThat(new AdminCoreAlertGateway(client, semSegredo).send(alerta())).isFalse();
        assertThat(semSegredo.isConfigured()).isFalse();
    }
}
