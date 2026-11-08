package com.businesspilot.module.auth;

import com.businesspilot.module.auth.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
class AuthIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("bp_test")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.jwt.secret",
            () -> "dGVzdC1zZWNyZXQtbXVzdC1iZS1hdC1sZWFzdC0yNTYtYml0cy1sb25nLWZvci10ZXN0cw==");
    }

    @Autowired WebApplicationContext wac;
    @Autowired ObjectMapper mapper;
    MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(wac)
            .apply(springSecurity())
            .build();
    }

    @Test
    void register_login_refresh_logout_scenario() throws Exception {
        // 1. Register
        var registerReq = new RegisterRequest(
            "test@example.com", "Password1234", "Jean", "Dupont", "Dupont SARL"
        );
        MvcResult registerResult = mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(registerReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").isNotEmpty())
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.role").value("OWNER"))
            .andReturn();

        AuthResponse registered = mapper.readValue(
            registerResult.getResponse().getContentAsString(), AuthResponse.class
        );
        assertThat(registered.organizationId()).isNotNull();

        // 2. Register duplicate → 422
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(registerReq)))
            .andExpect(status().isUnprocessableEntity());

        // 3. Login
        var loginReq = new LoginRequest("test@example.com", "Password1234");
        MvcResult loginResult = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(loginReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andReturn();

        AuthResponse loggedIn = mapper.readValue(
            loginResult.getResponse().getContentAsString(), AuthResponse.class
        );

        // 4. Wrong password → 422
        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new LoginRequest("test@example.com", "wrong"))))
            .andExpect(status().isUnprocessableEntity());

        // 5. Refresh
        mvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new RefreshRequest(loggedIn.refreshToken()))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty());

        // 6. Logout
        mvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new RefreshRequest(loggedIn.refreshToken()))))
            .andExpect(status().isNoContent());

        // 7. Refresh after logout → 422
        mvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new RefreshRequest(loggedIn.refreshToken()))))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void register_validation_fails_on_short_password() throws Exception {
        var req = new RegisterRequest("bad@example.com", "short", "A", "B", "Org");
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }
}
