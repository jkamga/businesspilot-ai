package com.businesspilot.module.knowledge;

import com.businesspilot.module.auth.dto.AuthResponse;
import com.businesspilot.module.auth.dto.RegisterRequest;
import java.util.UUID;
import com.businesspilot.module.knowledge.dto.KnowledgeEntryRequest;
import com.businesspilot.module.knowledge.domain.EntryType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
class KnowledgeIntegrationTest {

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

    String tokenOrg1;
    String tokenOrg2;

    @BeforeEach
    void setup() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(wac)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .build();

        tokenOrg1 = registerAndGetToken("org1." + UUID.randomUUID().toString().substring(0, 8) + "@test.com", "Org One");
        tokenOrg2 = registerAndGetToken("org2." + UUID.randomUUID().toString().substring(0, 8) + "@test.com", "Org Two");
    }

    @Test
    void crud_and_tenant_isolation() throws Exception {
        var req = new KnowledgeEntryRequest("Tarifs 2025", "Notre offre commence à 49$/mois", EntryType.TEXT);

        // Org1 crée une entrée
        MvcResult created = mvc.perform(post("/api/knowledge")
                .header("Authorization", "Bearer " + tokenOrg1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Tarifs 2025"))
            .andExpect(jsonPath("$.status").value("DRAFT"))
            .andReturn();

        var entry = mapper.readTree(created.getResponse().getContentAsString());
        String entryId = entry.get("id").asText();

        // Org1 voit ses entrées
        mvc.perform(get("/api/knowledge")
                .header("Authorization", "Bearer " + tokenOrg1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1));

        // Org2 ne voit pas les entrées d'Org1 — isolation tenant
        mvc.perform(get("/api/knowledge")
                .header("Authorization", "Bearer " + tokenOrg2))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(0));

        // Org1 publie l'entrée
        mvc.perform(post("/api/knowledge/" + entryId + "/publish")
                .header("Authorization", "Bearer " + tokenOrg1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ACTIVE"));

        // Org1 met à jour
        var updateReq = new KnowledgeEntryRequest("Tarifs 2025 MàJ", "Offre à partir de 39$/mois", EntryType.TEXT);
        mvc.perform(put("/api/knowledge/" + entryId)
                .header("Authorization", "Bearer " + tokenOrg1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Tarifs 2025 MàJ"));

        // Org2 ne peut pas accéder à l'entrée d'Org1
        mvc.perform(get("/api/knowledge/" + entryId)
                .header("Authorization", "Bearer " + tokenOrg2))
            .andExpect(status().isNotFound());

        // Org1 supprime
        mvc.perform(delete("/api/knowledge/" + entryId)
                .header("Authorization", "Bearer " + tokenOrg1))
            .andExpect(status().isNoContent());

        // Liste vide après suppression
        mvc.perform(get("/api/knowledge")
                .header("Authorization", "Bearer " + tokenOrg1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(0));
    }

    private String registerAndGetToken(String email, String orgName) throws Exception {
        var req = new RegisterRequest(email, "Password1234", "Test", "User", orgName);
        MvcResult result = mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }
}
