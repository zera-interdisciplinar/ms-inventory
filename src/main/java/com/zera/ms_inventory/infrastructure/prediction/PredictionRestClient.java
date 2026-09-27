package com.zera.ms_inventory.infrastructure.prediction;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Cliente HTTP do preditivo, montado aqui pelo mesmo motivo do admin-core: garantir o timeout e
 * nao depender de um builder autoconfigurado. Uma inferencia pendurada seguraria o job inteiro.
 */
final class PredictionRestClient {

    private PredictionRestClient() {
    }

    static RestClient of(PredictionProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) properties.timeout().toMillis());
        factory.setReadTimeout((int) properties.timeout().toMillis());

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(factory);
        if (!properties.apiKey().isBlank()) {
            // chamada via Kong; direto no servico do cluster o cabecalho nao e exigido
            builder.defaultHeader("apikey", properties.apiKey());
        }
        return builder.build();
    }
}
