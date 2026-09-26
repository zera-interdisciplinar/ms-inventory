package com.zera.ms_inventory.infrastructure.admincore;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Cria o cliente HTTP do admin-core com o timeout configurado. E montado aqui, e nao injetado, para
 * o servico nao depender da autoconfiguracao de um builder e para o timeout valer de fato: uma
 * chamada pendurada no job seguraria a execucao inteira.
 */
final class AdminCoreRestClient {

    private AdminCoreRestClient() {
    }

    static RestClient of(AdminCoreProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) properties.timeout().toMillis());
        factory.setReadTimeout((int) properties.timeout().toMillis());
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(factory)
                .build();
    }
}
