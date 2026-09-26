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
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(factory)
                .build();
    }
}
