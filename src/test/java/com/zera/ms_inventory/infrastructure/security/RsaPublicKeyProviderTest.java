package com.zera.ms_inventory.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import org.junit.jupiter.api.Test;

class RsaPublicKeyProviderTest {

    @Test
    void generatesEphemeralKeyWhenNoneConfigured() {
        RsaPublicKeyProvider provider = new RsaPublicKeyProvider(
                new JwtProperties("ms-administrative-core", "  "));

        assertThat(provider.publicKey()).isInstanceOf(RSAPublicKey.class);
    }

    @Test
    void parsesConfiguredPem() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        RSAPublicKey original = (RSAPublicKey) gen.generateKeyPair().getPublic();
        String pem = "-----BEGIN PUBLIC KEY-----\n"
                + Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(original.getEncoded())
                + "\n-----END PUBLIC KEY-----";

        RsaPublicKeyProvider provider = new RsaPublicKeyProvider(
                new JwtProperties("ms-administrative-core", pem));

        assertThat(provider.publicKey().getModulus()).isEqualTo(original.getModulus());
    }

    @Test
    void rejectsGarbagePem() {
        assertThatThrownBy(() -> new RsaPublicKeyProvider(
                new JwtProperties("x", "-----BEGIN PUBLIC KEY-----\nnope!!!\n-----END PUBLIC KEY-----")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void jwtPropertiesDefaultsIssuer() {
        assertThat(new JwtProperties(null, null).issuer()).isEqualTo("ms-administrative-core");
        assertThat(new JwtProperties("custom", null).issuer()).isEqualTo("custom");
    }
}
