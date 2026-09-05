package com.zera.ms_inventory.infrastructure.security;

import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Fornece a chave publica RSA usada para validar os access tokens emitidos pelo
 * ms-administrative-core. Vem de {@code zera.jwt.public-key} (PEM). Sem ela, gera uma chave
 * efemera e loga um WARN — nenhum token real passa a validar.
 */
@Component
public class RsaPublicKeyProvider {

    private static final Logger log = LoggerFactory.getLogger(RsaPublicKeyProvider.class);

    private final RSAPublicKey publicKey;

    public RsaPublicKeyProvider(JwtProperties properties) {
        if (properties.publicKey() != null && !properties.publicKey().isBlank()) {
            this.publicKey = parse(properties.publicKey());
        } else {
            log.warn("zera.jwt.public-key nao configurada — gerando chave efemera. "
                    + "Tokens reais do ms-administrative-core NAO serao validados.");
            this.publicKey = ephemeral();
        }
    }

    public RSAPublicKey publicKey() {
        return publicKey;
    }

    private static RSAPublicKey parse(String pem) {
        String normalized = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        try {
            return (RSAPublicKey) KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(normalized)));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IllegalArgumentException e) {
            throw new IllegalStateException("Chave publica RSA invalida (esperado X.509/SPKI PEM)", e);
        }
    }

    private static RSAPublicKey ephemeral() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return (RSAPublicKey) generator.generateKeyPair().getPublic();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("RSA indisponivel na JVM", e);
        }
    }
}
