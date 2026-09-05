package com.zera.ms_inventory.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuracao de validacao de JWT. A chave publica ({@code publicKey}, PEM X.509/SPKI) e a mesma
 * do ms-administrative-core, que emite os tokens. Sem ela, {@link RsaPublicKeyProvider} gera uma
 * chave efemera — util apenas para testes/dev, pois nenhum token real sera validado.
 */
@ConfigurationProperties(prefix = "zera.jwt")
public record JwtProperties(String issuer, String publicKey) {

    public JwtProperties {
        if (issuer == null || issuer.isBlank()) {
            issuer = "ms-administrative-core";
        }
    }
}
