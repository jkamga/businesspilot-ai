package com.businesspilot.module.conversations;

import com.businesspilot.module.auth.dto.AuthResponse;
import com.businesspilot.module.auth.dto.RegisterRequest;
import java.util.UUID;
import com.businesspilot.module.conversations.dto.ConversationRequest;
import com.businesspilot.module.conversations.dto.MessageRequest;
import com.businesspilot.module.conversations.service.AiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
class ConversationsIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("bp_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired WebApplicationContext wac;
    @Autowired ObjectMapper mapper;
    @MockitoBean AiService aiService;

    MockMvc mvc;
    String tokenOrg1;
    String tokenOrg2;

    @BeforeEach
    void setUp() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        when(aiService.generateResponse(any(), any(), any()))
                .thenReturn("Réponse de l'assistant IA.");

        tokenOrg1 = register("conv-org1@test.com", "ConvOrg1");
        tokenOrg2 = register("conv-org2@test.com", "ConvOrg2");
    }

    private String register(String email, String orgName) throws Exception {
        String uniqueEmail = email.replace("@", "." + UUID.randomUUID().toString().substring(0, 8) + "@");
        var req = new RegisterRequest(uniqueEmail, "password123!", "Test", "User", orgName);
        MvcResult result = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class).accessToken();
    }

    @Test
    void createAndListConversation() throws Exception {
        var req = new ConversationRequest("Jean Dupont", "jean@example.com");
        mvc.perform(post("/api/conversations")
                        .header("Authorization", "Bearer " + tokenOrg1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.prospectName").value("Jean Dupont"))
                .andExpect(jsonPath("$.status").value("OPEN"));

        mvc.perform(get("/api/conversations")
                        .header("Authorization", "Bearer " + tokenOrg1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(1)));
    }

    @Test
    void tenantIsolation() throws Exception {
        var req = new ConversationRequest("Prospect Org1", null);
        mvc.perform(post("/api/conversations")
                        .header("Authorization", "Bearer " + tokenOrg1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/conversations")
                        .header("Authorization", "Bearer " + tokenOrg2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void addMessageTriggersAiResponse() throws Exception {
        var createReq = new ConversationRequest("Client Test", "client@test.com");
        MvcResult created = mvc.perform(post("/api/conversations")
                        .header("Authorization", "Bearer " + tokenOrg1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        String convId = mapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

        mvc.perform(post("/api/conversations/" + convId + "/messages")
                        .header("Authorization", "Bearer " + tokenOrg1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new MessageRequest("Bonjour, j'ai une question."))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].role").value("USER"))
                .andExpect(jsonPath("$[1].role").value("ASSISTANT"))
                .andExpect(jsonPath("$[1].content").value("Réponse de l'assistant IA."));
    }

    @Test
    void resolveConversation() throws Exception {
        var req = new ConversationRequest(null, null);
        MvcResult created = mvc.perform(post("/api/conversations")
                        .header("Authorization", "Bearer " + tokenOrg1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        String convId = mapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

        mvc.perform(post("/api/conversations/" + convId + "/resolve")
                        .header("Authorization", "Bearer " + tokenOrg1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));
    }
}
