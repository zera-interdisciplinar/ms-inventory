package com.zera.ms_inventory.infrastructure.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.usecase.category.CreateCategory;
import com.zera.ms_inventory.core.usecase.rule.FindAllRules;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityRbacIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private CreateCategory createCategory;
    @MockitoBean private FindAllRules findAllRules;

    private static MockHttpServletRequestBuilder asRole(MockHttpServletRequestBuilder request, String role) {
        return request.with(jwt()
                .jwt(b -> b.subject(UUID.randomUUID().toString()))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    @Test
    void healthIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    void apiRejectsAnonymousRequests() throws Exception {
        mockMvc.perform(get("/api/v1/rules")).andExpect(status().isUnauthorized());
    }

    @Test
    void readsAllowedForAnyAuthenticatedRole() throws Exception {
        when(findAllRules.execute()).thenReturn(List.of());

        mockMvc.perform(asRole(get("/api/v1/rules"), "EMPLOYEE")).andExpect(status().isOk());
    }

    @Test
    void writesForbiddenForEmployee() throws Exception {
        mockMvc.perform(asRole(post("/api/v1/categories"), "EMPLOYEE")
                        .header("X-Unit-Id", Fixtures.UNIT.toString())
                        .contentType("application/json")
                        .content("{\"name\":\"Eletronicos\",\"description\":\"x\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void writesAllowedForManager() throws Exception {
        when(createCategory.execute(any(), any(), any(), any(), any()))
                .thenReturn(Fixtures.category(Fixtures.UNIT));

        mockMvc.perform(asRole(post("/api/v1/categories"), "MANAGER")
                        .header("X-Unit-Id", Fixtures.UNIT.toString())
                        .contentType("application/json")
                        .content("{\"name\":\"Eletronicos\",\"description\":\"x\"}"))
                .andExpect(status().isCreated());
    }
}
