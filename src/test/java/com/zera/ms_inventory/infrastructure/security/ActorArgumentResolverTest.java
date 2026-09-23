package com.zera.ms_inventory.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.context.request.ServletWebRequest;

import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.ActorRole;

class ActorArgumentResolverTest {

    private final UUID userId = UUID.randomUUID();

    @Test
    void shouldReadSubjectAndManagerRole() {
        TestingAuthenticationToken token = new TestingAuthenticationToken(userId.toString(), null, "ROLE_MANAGER");

        Actor actor = ActorArgumentResolver.fromAuthentication(token);

        assertThat(actor.userId()).isEqualTo(userId);
        assertThat(actor.role()).isEqualTo(ActorRole.MANAGER);
        assertThat(actor.isManager()).isTrue();
    }

    @Test
    void shouldTreatEveryOtherRoleAsEmployee() {
        TestingAuthenticationToken token = new TestingAuthenticationToken(userId.toString(), null, "ROLE_EMPLOYEE");

        assertThat(ActorArgumentResolver.fromAuthentication(token).role()).isEqualTo(ActorRole.EMPLOYEE);
    }

    @Test
    void shouldRejectMissingAuthentication() {
        TestingAuthenticationToken anonymous = new TestingAuthenticationToken(userId.toString(), null);
        anonymous.setAuthenticated(false);

        assertThatThrownBy(() -> ActorArgumentResolver.fromAuthentication(null))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
        assertThatThrownBy(() -> ActorArgumentResolver.fromAuthentication(anonymous))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    void shouldOnlySupportActorParameters() throws NoSuchMethodException {
        ActorArgumentResolver resolver = new ActorArgumentResolver();
        MethodParameter actorParam = new MethodParameter(
                ActorArgumentResolverTest.class.getDeclaredMethod("handler", Actor.class, String.class), 0);
        MethodParameter stringParam = new MethodParameter(
                ActorArgumentResolverTest.class.getDeclaredMethod("handler", Actor.class, String.class), 1);

        assertThat(resolver.supportsParameter(actorParam)).isTrue();
        assertThat(resolver.supportsParameter(stringParam)).isFalse();
    }

    @Test
    void shouldResolveFromTheRequestPrincipal() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setUserPrincipal(new TestingAuthenticationToken(userId.toString(), null, "ROLE_EMPLOYEE"));

        Actor actor = new ActorArgumentResolver().resolveArgument(null, null, new ServletWebRequest(request), null);

        assertThat(actor.userId()).isEqualTo(userId);
        assertThat(actor.role()).isEqualTo(ActorRole.EMPLOYEE);
    }

    @Test
    void shouldReadTheNameClaimFromTheJwt() {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "RS256").subject(userId.toString())
                .claim("role", "EMPLOYEE").claim("name", "Gustavo Macal").build();
        JwtAuthenticationToken token = new JwtAuthenticationToken(jwt,
                java.util.List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")), userId.toString());

        Actor actor = ActorArgumentResolver.fromAuthentication(token);

        assertThat(actor.name()).isEqualTo("Gustavo Macal");
        assertThat(actor.role()).isEqualTo(ActorRole.EMPLOYEE);
    }

    @SuppressWarnings("unused")
    private void handler(Actor actor, String other) {
    }
}
