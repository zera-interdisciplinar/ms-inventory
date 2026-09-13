package com.zera.ms_inventory.infrastructure.security;

import java.security.Principal;
import java.util.UUID;

import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.domain.valueobject.ActorRole;

/** Injeta o {@link Actor} autenticado (sub + role do JWT) como parametro dos controllers. */
@Component
public class ActorArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Actor.class.equals(parameter.getParameterType());
    }

    @Override
    public Actor resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                 NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        // o Spring Security expoe a Authentication como principal da requisicao
        Principal principal = webRequest.getUserPrincipal();
        Authentication authentication = principal instanceof Authentication fromRequest
                ? fromRequest
                : SecurityContextHolder.getContext().getAuthentication();
        return fromAuthentication(authentication);
    }

    static Actor fromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("An authenticated user is required");
        }
        boolean manager = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_MANAGER".equals(authority.getAuthority()));
        return new Actor(UUID.fromString(authentication.getName()), manager ? ActorRole.MANAGER : ActorRole.EMPLOYEE);
    }
}
