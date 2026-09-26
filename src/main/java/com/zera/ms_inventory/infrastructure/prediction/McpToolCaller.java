package com.zera.ms_inventory.infrastructure.prediction;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Chama uma ferramenta de um servidor MCP por HTTP.
 *
 * <p>O preditivo publica MCP em modo {@code stateless} com resposta JSON, entao uma chamada e um
 * unico POST JSON-RPC: nao ha sessao para abrir nem handshake para refazer a cada lote. Por isso
 * aqui nao entra um cliente MCP completo — ele traria ciclo de vida, reconexao e um bean que
 * tentaria falar com o servico ainda na subida, justamente o que esta integracao evita ao nascer
 * desligada.</p>
 */
class McpToolCaller {

    private static final String PROTOCOL_VERSION = "2025-06-18";

    private final RestClient restClient;
    private final JsonMapper jsonMapper;
    private final String endpoint;
    private final AtomicLong nextId = new AtomicLong(1);

    McpToolCaller(RestClient restClient, JsonMapper jsonMapper, String endpoint) {
        this.restClient = restClient;
        this.jsonMapper = jsonMapper;
        this.endpoint = endpoint;
    }

    /**
     * O valor de retorno da ferramenta. Servidor MCP responde o resultado tipado em
     * {@code structuredContent}; quando nao o faz, o mesmo valor vem serializado como texto em
     * {@code content}, e e de la que lemos.
     */
    JsonNode call(String tool, Map<String, Object> arguments) {
        Map<String, Object> request = Map.of(
                "jsonrpc", "2.0",
                "id", nextId.getAndIncrement(),
                "method", "tools/call",
                "params", Map.of("name", tool, "arguments", arguments));

        JsonNode response = restClient.post()
                .uri(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                // o transporte streamable aceita os dois; sem este Accept o servidor recusa o POST
                .header("Accept", "application/json, text/event-stream")
                .header("MCP-Protocol-Version", PROTOCOL_VERSION)
                .body(request)
                .retrieve()
                .body(JsonNode.class);

        if (response == null) {
            throw new McpCallException(tool + " returned an empty response");
        }
        if (response.hasNonNull("error")) {
            throw new McpCallException(tool + " failed: " + response.get("error").toString());
        }

        JsonNode result = response.path("result");
        if (result.path("isError").asBoolean(false)) {
            throw new McpCallException(tool + " reported an error: " + textContent(result));
        }

        JsonNode structured = result.path("structuredContent");
        if (!structured.isMissingNode() && !structured.isNull()) {
            // uma ferramenta que devolve lista vem embrulhada em {"result": [...]}
            return structured.has("result") ? structured.get("result") : structured;
        }
        return parse(textContent(result), tool);
    }

    private static String textContent(JsonNode result) {
        JsonNode content = result.path("content");
        if (content.isArray() && !content.isEmpty()) {
            return content.get(0).path("text").asText("");
        }
        return content.toString();
    }

    private JsonNode parse(String text, String tool) {
        try {
            return jsonMapper.readTree(text);
        } catch (Exception e) {
            throw new McpCallException(tool + " returned a payload that is not JSON: " + text, e);
        }
    }

    static class McpCallException extends RuntimeException {
        McpCallException(String message) {
            super(message);
        }

        McpCallException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
